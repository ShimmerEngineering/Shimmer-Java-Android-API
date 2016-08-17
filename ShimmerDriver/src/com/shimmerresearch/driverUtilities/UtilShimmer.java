package com.shimmerresearch.driverUtilities;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.commons.lang3.ArrayUtils;

/** Utility class with commonly useful methods
 * 
 * @author Mark Nolan
 *
 */
public class UtilShimmer implements Serializable {
	
	/** * */
	private static final long serialVersionUID = -3892204042703820796L;
	
	public String mParentClassName = "UpdateCheck";
	public Boolean mDebugMode = true;
	public Boolean mVerboseMode = true;
	
	public static final String STRING_CONSTANT_FOR_UNKNOWN = "Unknown";
	public static final String MAC_ADDRESS_ZEROS = "000000000000";
	public static final String MAC_ADDRESS_FFFFS = "FFFFFFFFFFFF";

	public UtilShimmer(String parentClassName, Boolean verboseMode){
		this.mParentClassName = parentClassName;
		this.mVerboseMode = verboseMode;
	}
	
	public UtilShimmer(String parentClassName, boolean verboseMode, boolean debugMode) {
		this(parentClassName, verboseMode);
		this.mDebugMode = debugMode;
	}

	public void consolePrintLn(String message) {
		if(mVerboseMode) {
			Calendar rightNow = Calendar.getInstance();
			String rightNowString = "[" + String.format("%02d",rightNow.get(Calendar.HOUR_OF_DAY)) 
					+ ":" + String.format("%02d",rightNow.get(Calendar.MINUTE)) 
					+ ":" + String.format("%02d",rightNow.get(Calendar.SECOND)) 
					+ ":" + String.format("%03d",rightNow.get(Calendar.MILLISECOND)) + "]";
			System.out.println(rightNowString + " " + mParentClassName + ": " + message);
		}		
	}
	public void consolePrint(String message) {
		if(mVerboseMode) {
			System.out.print(message);
		}		
	}

	public void consolePrintLnDebug(String message) {
		if(mDebugMode){
			consolePrintLn(message);
		}
	}

	public void setVerboseMode(boolean verboseMode) {
		mVerboseMode = verboseMode;
	}

	public static String convertSecondsToDateString(long seconds) {
		return convertMilliSecondsToDateString(seconds * 1000);
	}
	
	public static String convertMilliSecondsToDateString(long milliSeconds) {
		Calendar cal = Calendar.getInstance();
		
		cal.setTimeInMillis(milliSeconds);
		int dayIndex = cal.get(Calendar.DAY_OF_MONTH);
		String dayString = getDayOfMonthSuffix(dayIndex);

		int monthIndex = cal.get(Calendar.MONTH);
		String monthString = getMonthString(monthIndex);
		
    	DateFormat dfLocal = new SimpleDateFormat("//yyyy HH:mm:ss");
    	String timeString = dfLocal.format(new Date(milliSeconds));
    	timeString = timeString.replaceFirst("//", dayIndex + dayString + " " + monthString + " ");
		return timeString;
	}
	
	public static String convertMilliSecondsToHrMinSecString(long milliSeconds) {
		DateFormat dfLocal = new SimpleDateFormat("HH:mm:ss");
		dfLocal.setTimeZone(TimeZone.getTimeZone("UTC"));
		// int style = DateFormat.MEDIUM;
		// dfLocal = DateFormat.getDateInstance("HH:mm:ss", Locale.UK);
		String timeString = dfLocal.format(new Date(milliSeconds));
		return timeString;
	}
	
	private static String getDayOfMonthSuffix(final int n) {
	    if (n >= 11 && n <= 13) {
	        return "th";
	    }
	    switch (n % 10) {
	        case 1:  return "st";
	        case 2:  return "nd";
	        case 3:  return "rd";
	        default: return "th";
	    }
	}
	
	
	public static String convertBytes(double bytes){
		
		bytes = (double)((bytes) / 1024 / 1024 / 1024);
	    
	    String mFormattedBytesTxt = " GB";
	    
    	if(bytes < 0.001){
    		bytes = bytes * 1024 * 1024;
    		mFormattedBytesTxt = " KB";
    	}	
	    
    	else if (bytes < 1.0) {
	    	bytes = bytes * 1024;
	    	mFormattedBytesTxt = " MB";
	    }
	    mFormattedBytesTxt = String.format("%.2f", bytes) + mFormattedBytesTxt;
	    return mFormattedBytesTxt;
	    
	}
	
	public static boolean isNumeric(String str){
		if(str==null) {
			return false;
		}
		if(str.isEmpty()) {
			return false;
		}
		
	    for (char c : str.toCharArray()){
	        if (!Character.isDigit(c)) return false;
	    }
	    return true;
	}
	
	
	public static boolean isAsciiPrintable(char ch) {
	      return ch >= 32 && ch < 127;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHexString(byte[] bytes) {
		if(bytes!=null){
		    char[] hexChars = new char[bytes.length * 2];
		    for ( int j = 0; j < bytes.length; j++ ) {
		        int v = bytes[j] & 0xFF;
		        hexChars[j * 2] = hexArray[v >>> 4];
		        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		    }
		    return new String(hexChars);
		}
		else {
			return null;
		}
	}
	
	public static String byteToHexString(byte bytes) {
	    char hexChars[] = new char[2];
        int v = bytes & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];
	    return new String(hexChars);
	}
	
	public static String bytesToHexStringWithSpaces(byte[] bytes) {
		if(bytes!=null){
		    char[] hexChars = new char[bytes.length * 3];
		    for ( int j = 0; j < bytes.length; j++ ) {
		        int v = bytes[j] & 0xFF;
		        hexChars[j * 3] = hexArray[v >>> 4];
		        hexChars[j * 3 + 1] = hexArray[v & 0x0F];
		        hexChars[j * 3 + 2] = ' ';
		    }
		    return new String(hexChars);
		}
		else {
			return null;
		}
	}
	
	public static String byteToHexStringFormatted(byte bytes) {
		int charCntPerByte = 4;
	    char hexChars[] = new char[charCntPerByte];
        int v = bytes & 0xFF;
        hexChars[0] = '0';
        hexChars[1] = 'x';
        hexChars[2] = hexArray[v >>> 4];
        hexChars[3] = hexArray[v & 0x0F];
	    String returnString = new String(hexChars);
	    returnString = "[" + returnString + "]";
	    return returnString;
	}

	public static byte[] intToByteArrayLsb(int intToUse, int numBytes) {
		byte[] buffer = new byte[numBytes]; 
		for(int i=0;i<numBytes;i++) {
			buffer[i] = (byte) (intToUse >> (i*8));
		}
	    return buffer;
	}

	public static byte[] intToByteArrayMsb(int intToUse, int numBytes) {
		byte[] buffer = new byte[numBytes]; 
		for(int i=0;i<numBytes;i++) {
			buffer[i] = (byte) (intToUse >> ((numBytes-1-i)*8));
		}
	    return buffer;
	}

	public static String bytesToHexStringWithSpacesFormatted(byte[] bytes) {
		if(bytes!=null && bytes.length>0){
			int charCntPerByte = 5;
		    char[] hexChars = new char[(bytes.length * charCntPerByte)-1];
		    for ( int j = 0; j < bytes.length; j++ ) {
		        int v = bytes[j] & 0xFF;
		        hexChars[j * charCntPerByte] = '0';
		        hexChars[j * charCntPerByte + 1] = 'x';

		        hexChars[j * charCntPerByte + 2] = hexArray[v >>> 4];
		        hexChars[j * charCntPerByte + 3] = hexArray[v & 0x0F];
		        if(j!=bytes.length-1){
			        hexChars[j * charCntPerByte + 4] = ' ';
		        }
		    }
		    String returnString = new String(hexChars);
		    returnString = "[" + returnString + "]";
		    return returnString;
		}
		else {
			return null;
		}
	}	
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[1];
	    if(s.toCharArray().length == 0){
	    	data[0] = 0;
	    }
	    else if(s.toCharArray().length == 1){
	    	data[0] = (byte) Character.digit(s.charAt(0), 16); 
	    }
	    else {
	    	data = new byte[len / 2];
		    for (int i = 0; i < len; i += 2) {
		        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
		                             + Character.digit(s.charAt(i+1), 16));
		    }
	    }
	    return data;
	}

	public static int hexStringToInt(String s){
		int len = s.length();
		int data = 0;
		int radix = 16;

		if(len == 0){
			data = 0;	
		}
		else if(len <= 8){	

			//max value fitting in int: "7FFFFFFF" (= 2^31-1)
			if(len == 8 && Integer.parseInt(s.substring(0,1), radix) > 7){
				data = 0;	
			}
			else{
				data = Integer.parseInt(s, radix);
			}
		}
		return data;

	}
	
	public static String convertByteToUnsignedIntegerString(byte b) {
		return Integer.toString(b&0x00FF);
	}

	/**Returns true if FW ID and HW_ID are the same and "this" version is greater or equal then comparison version
	 * @param thisFwIdent
	 * @param thisMajor
	 * @param thisMinor
	 * @param thisInternal
	 * @param compFwIdent
	 * @param compMajor
	 * @param compMinor
	 * @param compInternal
	 * @return
	 */
	public static boolean compareVersions(int thisHwIdent, int thisFwIdent, int thisMajor, int thisMinor, int thisInternal,
			int compHwIdent, int compFwIdent, int compMajor, int compMinor, int compInternal) {

		if(compHwIdent!=ShimmerVerDetails.ANY_VERSION){
			if (thisHwIdent!=compHwIdent){
				return false;
			}
		}
		return compareVersions(thisFwIdent, thisMajor, thisMinor, thisInternal, compFwIdent, compMajor, compMinor, compInternal);

//		if (thisHwIdent==compHwIdent){
//			return compareVersions(thisFwIdent, thisMajor, thisMinor, thisInternal, compFwIdent, compMajor, compMinor, compInternal);
//		}
//		return false; // if less or not the same FW_ID and HW_ID
	}

	
	/**Returns true if FW ID is the same and "this" version is greater or equal then comparison version
	 * @param thisFwIdent
	 * @param thisMajor
	 * @param thisMinor
	 * @param thisInternal
	 * @param compFwIdent
	 * @param compMajor
	 * @param compMinor
	 * @param compInternal
	 * @return
	 */
	public static boolean compareVersions(int thisFwIdent, int thisMajor, int thisMinor, int thisInternal,
			int compFwIdent, int compMajor, int compMinor, int compInternal) {

		if(compFwIdent!=ShimmerVerDetails.ANY_VERSION){
			if (thisFwIdent!=compFwIdent){
				return false;
			}
		}
		return compareVersions(thisMajor, thisMinor, thisInternal, compMajor, compMinor, compInternal);
		
//		if (thisFwIdent==compFwIdent){
//			return compareVersions(thisMajor, thisMinor, thisInternal, compMajor, compMinor, compInternal);
//		}
//		return false; // if less or not the same FW ID
	}
	
	/**Returns true if "this" version is greater or equal then comparison version
	 * @param thisMajor
	 * @param thisMinor
	 * @param thisInternal
	 * @param compMajor
	 * @param compMinor
	 * @param compInternal
	 * @return
	 */
	public static boolean compareVersions(int thisMajor, int thisMinor, int thisInternal,
			int compMajor, int compMinor, int compInternal) {

		if ((thisMajor>compMajor)
				||(thisMajor==compMajor && thisMinor>compMinor)
				||(thisMajor==compMajor && thisMinor==compMinor && thisInternal>=compInternal)){
			return true; // if FW ID is the same and version is greater or equal 
		}
		return false; // if less or not the same FW ID
	}
	
	public static String convertDuration(int duration){
		
		double totalSecs = duration/1000; //convert from miliseconds to seconds
		int hours = (int) (totalSecs / 3600);
		int minutes = (int) ((totalSecs % 3600) / 60);
		int seconds = (int) (totalSecs % 60);
		String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		
		return timeString;
	}
	
	
	public String fromMilToDate(double miliseconds){
		
		long mili = (long) miliseconds;
		Date date = new Date(mili);		
		DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return formatter.format(date);
	}
	
	
	public String fromSecondsToDate(String seconds){
		
		double miliseconds = 1000*Double.valueOf(seconds);
		long mili = (long) miliseconds;
		Date date = new Date(mili);		
		DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return formatter.format(date);
	}
	
	public static String fromMilToDateExcelCompatible(String miliseconds, Boolean showMillis){
		
		double miliInDouble = Double.parseDouble(miliseconds);
		long mili = (long) miliInDouble;
		Date date = new Date(mili);
		DateFormat formatter;
		if(showMillis){
			formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		}
		else{
			formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		}
		
		return formatter.format(date);
	}
	
	public static File[] getArrayOfFilesWithFileType(File directory, final String fileType){
		File[] listOfFiles = directory.listFiles(new FilenameFilter() {
	        @Override
	        public boolean accept(File dir, String fileName) {
	        	String extension = "";
	        	int i = fileName.lastIndexOf('.');
	        	int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
	        	if (i > p) {
	        	    extension = fileName.substring(i+1);
	        	}
//	            return extension.matches("txt");
	        	return extension.matches(fileType);
	        }
	    });
		return listOfFiles;
	}
	
	
	public static boolean stringContainsItemFromListUpperCaseCheck(String inputString, String[] items) {
	    for(int i =0; i < items.length; i++) {
	        if(inputString.toUpperCase().contains(items[i].toUpperCase())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static boolean stringContainsItemFromList(String inputString, String[] items) {
	    for(int i =0; i < items.length; i++) {
	        if(inputString.contains(items[i])) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public void millisecondDelay(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			consolePrintLn("Thread sleep FAIL");
		//	e.printStackTrace();
	}
}

	/**Used by the RTC sent over Bluetooth/Dock comms and calibration Dump file
	 * @param milliseconds
	 * @return
	 */
	public static byte[] convertMilliSecondsToShimmerRtcDataBytesLSB(long milliseconds) {
		byte[] rtcTimeArray = convertMilliSecondsToShimmerRtcDataBytesMSB(milliseconds);
		ArrayUtils.reverse(rtcTimeArray); // Big-endian by default
		return rtcTimeArray;
	}

	/** 
	 * @param milliseconds
	 * @return
	 */
	public static byte[] convertMilliSecondsToShimmerRtcDataBytesMSB(long milliseconds) {
		long milisecondTicks = (long)(((double)milliseconds) * 32.768); // Convert miliseconds to clock ticks
		byte[] rtcTimeArray = ByteBuffer.allocate(8).putLong(milisecondTicks).array();
		return rtcTimeArray;
	}

	/**Used by the RTC sent over Bluetooth/Dock comms and calibration Dump file
	 * @param rtcTimeArray
	 * @return
	 */
	public static long convertShimmerRtcDataBytesToMilliSecondsLSB(byte[] rtcTimeArray) {
		byte[] reversedArray = ArrayUtils.addAll(rtcTimeArray, null); //Create a clone
		ArrayUtils.reverse(reversedArray); // Big-endian by default
		return convertShimmerRtcDataBytesToMilliSecondsMSB(reversedArray);
	}

	/**
	 * @param rtcTimeArray
	 * @return
	 */
	public static long convertShimmerRtcDataBytesToMilliSecondsMSB(byte[] rtcTimeArray) {
		long timeWrapped = ByteBuffer.wrap(rtcTimeArray).getLong();
		long milisecondTicks = (long)((((double)timeWrapped)/32.768));  // Convert clock ticks to milliseconds
		return milisecondTicks;
	}

	/**Joins all string in array, each separated by a space 
	 * @param a an array of Strings to join
	 * @return
	 */
	public static String joinStrings(String[] a){
		String js="";
		for (int i=0;i<a.length;i++){
			if (i==0){
				js = a[i];
			} else{
				js = js + " " + a[i];
			}
		}
		return js;
	}

	public static double[][] deepCopyDoubleMatrix(double[][] input) {
	    if (input == null)
	        return null;
	    double[][] result = new double[input.length][];
	    for (int r = 0; r < input.length; r++) {
	        result[r] = input[r].clone();
	    }
	    return result;
	}
	
	
	
	public static ArrayList<Double> sortByComparator(Map<Double,Double> unsortMap, final boolean order) {
        List<Entry<Double, Double>> list = new LinkedList<Entry<Double, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<Double,Double>>(){ 
			@Override
			public int compare(Entry<Double, Double> o1,
					Entry<Double, Double> o2) {
				if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else {
                    return o2.getValue().compareTo(o1.getValue());
                }
			}
        });

        // Maintaining insertion order with the help of LinkedList
        Map<Double,Double> sortedMap = new LinkedHashMap<Double,Double>();
        for (Entry<Double,Double> entry : list){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return new ArrayList<Double>(sortedMap.keySet());
    }

	public static boolean isValidMac(String mac) {
		if(mac==null){
			return false;
		}

		if(mac.isEmpty()){
			return false;
		}

		if(mac.equals(MAC_ADDRESS_FFFFS) || mac.equals(MAC_ADDRESS_ZEROS)){
			return false;
		}
		
		return true;
	}

	public static String getDayString(int dayIndex) {
		switch (dayIndex) {
		    case Calendar.SUNDAY:
		    	return "Sun";
		    case Calendar.MONDAY:
		    	return "Mon";
		    case Calendar.TUESDAY:
		    	return "Tue";
		    case Calendar.WEDNESDAY:
		    	return "Wed";
		    case Calendar.THURSDAY:
		    	return "Thur";
		    case Calendar.FRIDAY:
		    	return "Fri";
		    case Calendar.SATURDAY:
		    	return "Sat";
		    default:
		    	return "";
		}
	}
	
	private static String getMonthString(int monthIndex) {
    	switch(monthIndex){
			case(Calendar.JANUARY):
				return "Jan";
			case(Calendar.FEBRUARY):
				return "Feb";
			case(Calendar.MARCH):
				return "Mar";
			case(Calendar.APRIL):
				return "Apr";
			case(Calendar.MAY):
				return "May";
			case(Calendar.JUNE):
				return "June";
			case(Calendar.JULY):
				return "July";
			case(Calendar.AUGUST):
				return "Aug";
			case(Calendar.SEPTEMBER):
				return "Sept";
			case(Calendar.OCTOBER):
				return "Oct";
			case(Calendar.NOVEMBER):
				return "Nov";
			case(Calendar.DECEMBER):
				return "Dec";
            default:
            	return "";
    	}
	}
	
	
	public static File createFileAndDeleteIfExists(String filePath){
		
		File fileOld = new File(filePath);
		File fileNew = null;
		
		if(fileOld.exists()){
			fileOld.delete();
			fileNew = new File(filePath);
		}
		else{
			fileNew = fileOld;
		}
		
		return fileNew;
	}

	public static byte[] generateRadioConfigByteArray(int radioChannel, int radioGroupId, int radioDeviceId, int radioResponseWindow) {
		byte[] radioConfigArray = new byte[7];
		
        radioConfigArray[0] = (byte)((radioChannel >> 0) & 0x00FF);
        
        //All MSB first
        radioConfigArray[1] = (byte)((radioGroupId >> 8) & 0x00FF);
        radioConfigArray[2] = (byte)((radioGroupId >> 0) & 0x00FF);
        radioConfigArray[3] = (byte)((radioDeviceId >> 8) & 0x00FF);
        radioConfigArray[4] = (byte)((radioDeviceId >> 0) & 0x00FF);
        radioConfigArray[5] = (byte)((radioResponseWindow >> 8) & 0x00FF);
        radioConfigArray[6] = (byte)((radioResponseWindow >> 0) & 0x00FF);
        
		return radioConfigArray;
	}

	
	public static String getConfigTimeFromFullTrialName(String trialName){
		
		String[] splittedTrialName = trialName.split("_");
		String configTime = splittedTrialName[splittedTrialName.length-1];
		return configTime;
	}

	
	public static String convertStackTraceToString(StackTraceElement[] exceptionStackTrace){
		Exception e = new Exception();
		e.setStackTrace(exceptionStackTrace);
//		e.printStackTrace();
		
//		for(StackTraceElement element:msg.mExceptionStackTrace) {
//			consolePrint("Exception thrown from " + element.getMethodName()
//              + " in class " + element.getClassName() + " [on line number "
//              + element.getLineNumber() + " of file " + element.getFileName() + "\n");
//		}
		
		//create new StringWriter object
		StringWriter sWriter = new StringWriter();
		//create PrintWriter for StringWriter
		PrintWriter pWriter = new PrintWriter(sWriter);
		//now print the stacktrace to PrintWriter we just created
		e.printStackTrace(pWriter);
		//use toString method to get stacktrace to String from StringWriter object
		String strStackTrace = sWriter.toString();
		return strStackTrace;
	}

	public static String longToHexStringWithSpacesFormatted(long number, int numBytes) {
		byte[] bytesArray = new byte[numBytes];
		for(int i=0;i<numBytes;i++){
			bytesArray[i] = (byte) ((number >> (i*8)) & 0xFF);
		}
		return bytesToHexStringWithSpacesFormatted(bytesArray);
	}

	public static String doubleArrayToString(double[][] doubleArray) {
		String returnString = "";
		for(int x=0;x<doubleArray.length;x++){
			for(int y=0;y<doubleArray[x].length;y++){
				returnString += doubleArray[x][y] + "\t";
			}
			returnString += "\n";
		}
		return returnString;
	}

	public static double[][] nudgeDoubleArray(double maxVal, double minVal, int precision, double[][] newArray) {
		for(int x=0;x<newArray.length;x++){
			for(int y=0;y<newArray[x].length;y++){
				//nudge into range
				newArray[x][y] = UtilShimmer.nudgeDouble(newArray[x][y], minVal, maxVal);
				//correct the precision
				newArray[x][y] = applyPrecisionCorrection(newArray[x][y], precision);
			}
		}
		return newArray;
	}
	
	public static double applyPrecisionCorrection(double value, int precision) {
		return new BigDecimal(value).setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

	public static double nudgeDouble(double valToNudge, double minVal, double maxVal) {
//		valToNudge = (valToNudge>maxVal? maxVal:valToNudge);
//		valToNudge = (valToNudge<minVal? minVal:valToNudge);
//		return valToNudge;
	    return Math.max(minVal, Math.min(maxVal, valToNudge));
	}
	
	public static int nudgeInteger(int valToNudge, int minVal, int maxVal) {
	    return Math.max(minVal, Math.min(maxVal, valToNudge));
	}


	public static boolean isAllZeros(double[][] matrix){
		if(matrix==null){
			return false;
		}

		boolean allZeros = true;
		for(int j = 0; j < matrix[1].length; j++){
			for(int i = 0; i < matrix.length; i++){
				if(matrix[j][i]!=0){
					return false;
				}
			}
		}
		return allZeros;
	}

	public static boolean isAllFF(byte[] bufferCalibrationParameters) {
		for(byte myByte:bufferCalibrationParameters){
			if(myByte!=(byte)0xFF){
				return false;
			}
		}
		return true;
	}
	
	public static boolean isAllZeros(byte[] bufferCalibrationParameters) {
		for(byte myByte:bufferCalibrationParameters){
			if(myByte!=(byte)0x00){
				return false;
			}
		}
		return true;
	}

	
}
