package com.shimmerresearch.verisense;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class UtilVerisenseDriver implements Serializable {

	private static final long serialVersionUID = 3008337300454867021L;
	
	//	public static final String TIME_ZONE_STR = "UTC";
	public static final int TIME_ZONE_OFFSET = +0;
	public static final String TIME_ZONE_STR = "GMT+" + TIME_ZONE_OFFSET;
	//TODO pick and use only one library for time (i.e., SimpleDateFormat vs. LocalDate)
	public static final TimeZone TIME_ZONE = TimeZone.getTimeZone(TIME_ZONE_STR);
	public static final ZoneId TIME_ZONE_ID = ZoneId.of(TIME_ZONE_STR);
//	public static final ZoneId TIME_ZONE_ID = ZoneId.systemDefault();

	public static final String CSV_DELIMITER = ",";
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String FEATURE_NOT_AVAILABLE = "N/A";

	public static final String DATE_FORMAT_VERISENSE_HEADER_LEGACY = "dd/MM/yyyy HH:mm:ss.SSS";
	public static final String DATE_FORMAT_VERISENSE_HEADER = "yyyy/MM/dd HH:mm:ss.SSS";
	public static final String DATE_FORMAT_DEBUGGING = "yyyy/MM/dd HH:mm:ss.SSS";
	public static final String DATE_FORMAT_NO_MILLIS = "yyyy/MM/dd HH:mm:ss";
	public static final String DATE_FORMAT_FILENAME = "yyMMdd_HHmmss";
	public static final String DATE_FORMAT_IN_DB = "dd MMM yyyy HH:mm:ss:SSS";
	public static final String DATE_FORMAT_SHORT_WITH_MILLIS = "yyMMdd HH:mm:ss.SSS";
	public static Calendar calendar = Calendar.getInstance(UtilVerisenseDriver.TIME_ZONE);
	//Text is in the format of "yyyy-MM-dd HH:mm:ss+SSSS" where SSSS is always zero. The Z sets the timezone to be UTC
	public static SimpleDateFormat sdfGgir = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
	
	public static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyMMdd");
	public static final DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern(DATE_FORMAT_FILENAME);
	
	public static final LocalTime LT23_59 = LocalTime.of(23, 59, 59, 999999999);

	//TODO duplication with above?
	private static DateFormat dfForFileNameUtc = new SimpleDateFormat(DATE_FORMAT_FILENAME);
	
	// 40 x '-'
	public static final String CSV_HEADER_SEPARATOR = "----------------------------------------";
	public static final String CSV_HEADER_SEPARATOR_LEGACY = "--------------------------------------------------,,\"";
	/*
	 * The number of header lines used to be fixed but now, with support for
	 * multiple sensors being enabled, the number of lines can vary. For a long time
	 * the line qty was 12 lines and we used to add 5 to allow for future expansion.
	 * There is now an additional line for each additional enabled sensor.
	 */
	public static final int CSV_HEADER_LINES_SEARCH_LIMIT = 12+5;
	
	public static final String FILE_FILTER_69 = "69";
	public static final String FILE_FILTER_70 = "70";
	public static final String FILE_FILTER_PREFIX_69 = File.separator + FILE_FILTER_69;
	public static final String FILE_FILTER_PREFIX_70 = File.separator + FILE_FILTER_70;
	public static final String FILE_FILTER_DUPLICATE = ").";

	//TODO move these to UtilShimmer.ENUM_FILE_FORMAT?
	public class FILE_EXTENSION {
		public static final String JSON = ".json";
		public static final String BIN = ".bin";
		public static final String ZIP = ".zip";
		public static final String CSV = ".csv";
		public static final String VLOG = ".vlog";
	}

	public class ASM_DIRECTORY_NAMES {
		public static final String BINARY_FILES = "BinaryFiles";
		public static final String PARSED_FILES = "ParsedFiles";
		public static final String CALIBRATION_PARAMETERS = "CalibrationParameters";
		public static final String ALGORITHMS = "Algorithms";
		public static final String SUMMARY = "Summary";
	}
	
	public static final String UNAVAILABLE = "Unavailable";
	
	public static final String BAD_CRC_IN_FILE_NAME = "_BadCRC";

	/** Expects path like: "verisense-unit-test-data/0001/Participant001/Verisense-18092601B10B/BinaryFiles/190219_144252_00000.bin"
	 *  Returns: "verisense-unit-test-data/0001/Participant001/Verisense-18092601B10B"
	 * 
	 * @param binaryFilePath
	 * @return
	 */
	public static String binaryFileDirToDeviceLevelDir(String binaryFilePath) {
		File file = new File(binaryFilePath);
		File parentFile = file.getParentFile();
		String parent = parentFile.getParent(); 
		return parent;
	}
	
	public static String binaryFileDirToParsedFileDir(String binaryFilePath) {
		//TODO working for Lambda function but not working for local parser
//		return (binaryFileDirToParentDir(binaryFilePath) + "/" + ASM_DIRECTORY_NAMES.PARSED_FILES);
		
		File file = new File(binaryFilePath);
		return (binaryFileDirToParentDir(binaryFilePath) + ASM_DIRECTORY_NAMES.PARSED_FILES);
	}

	public static String binaryFileDirToParentDir(String binaryFilePath) {
		File file = new File(binaryFilePath);
		return (file.getParentFile() +"/");
	}

	public static String payloadIndexToString(int startPayloadIndex) {
		return String.format("%05d", startPayloadIndex);
	}

	/**
	 * @param timestampMs1
	 * @param timestampMs2
	 * @return true if one timestampMs1 is before/after mid-day while timestampMs2 is after/before mid-day (i.e., a transition is present)
	 */
	public static boolean isTransitionMidDayOrMidnight(double timestampMs1, double timestampMs2) {
		return (UtilVerisenseDriver.isTransistionMidday(timestampMs1, timestampMs2)
				|| UtilVerisenseDriver.isTransistionMidnight(timestampMs1, timestampMs2));
	}
	
	/**
	 * @param timestampMs1
	 * @param timestampMs2
	 * @return true if timestampMs1 <12:00pm and timestampMs2 is >=12:00pm
	 */
	public static boolean isTransistionMidday(double timestampMs1, double timestampMs2) {
		if(UtilVerisenseDriver.isTsBeforeMidday((long) timestampMs1) 
				&& UtilVerisenseDriver.isTsAfterMidday((long) timestampMs2)) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param timestampMs1
	 * @param timestampMs2
	 * @return True if timestampMs1 >12:00pm and timestampMs2 is <=12:00pm 
	 */
	public static boolean isTransistionMidnight(double timestampMs1, double timestampMs2) {
		if(UtilVerisenseDriver.isTsAfterMidday((long) timestampMs1) 
				&& UtilVerisenseDriver.isTsBeforeMidday((long) timestampMs2)) {
			return true;
		}
		return false;
	}

	public static boolean isTsBeforeMidday(long millis) {
		long hours = millisToHrOfDay(millis);
		return (hours<12);
	}

	public static boolean isTsAfterMidday(long millis) {
		long hours = millisToHrOfDay(millis);
		return (hours>=12);
	}

	public static long millisToHrOfDay(long millis) {
		return millisToPartOfDay(millis, Calendar.HOUR_OF_DAY);
	}

	public long millisToMinutesOfHour(long millis) {
		return millisToPartOfDay(millis, Calendar.MINUTE);
	}

	public static long millisToSecondsOfMinute(long millis) {
		return millisToPartOfDay(millis, Calendar.SECOND);
	}

	public static long millisToPartOfDay(long millis, int scale) {
		calendar.setTime(new Date(millis));
		long minutes = calendar.get(scale);
		return minutes;
	}
	
	public Calendar toCalendar(long millis) {
		calendar.setTime(new Date(millis));
		return calendar;
	}
	
	public static String timeMsToString(double timeMs) {
		return timeMsToString((long)timeMs);
	}

	public static String timeMsToString(long timeMs) {
		dfForFileNameUtc.setTimeZone(UtilVerisenseDriver.TIME_ZONE);
		String timeString = dfForFileNameUtc.format(new Date(timeMs));
		return timeString;
	}

	public static String convertMilliSecondsToDateString(long milliSeconds, boolean showMillis) {
		return convertMilliSecondsToDateString(milliSeconds, showMillis, UtilVerisenseDriver.TIME_ZONE_STR);
	}

	//TODO move this to UtilShimmer
	public static String convertMilliSecondsToDateString(long milliSeconds, boolean showMillis, String timezone) {
		Calendar cal = Calendar.getInstance();
		
		cal.setTimeInMillis(milliSeconds);
		int dayIndex = cal.get(Calendar.DAY_OF_MONTH);
		String dayString = getDayOfMonthSuffix(dayIndex);

		int monthIndex = cal.get(Calendar.MONTH);
		String monthString = getMonthString(monthIndex);
		
		String simpleDateFormat = showMillis ? "//yyyy HH:mm:ss.SSS" : "//yyyy HH:mm:ss";
		
		DateFormat dfLocal = new SimpleDateFormat(simpleDateFormat);
		dfLocal.setTimeZone(TimeZone.getTimeZone(timezone));
		String timeString = dfLocal.format(new Date(milliSeconds));
		timeString = timeString.replaceFirst("//", dayIndex + dayString + " " + monthString + " ");
		return timeString;
	}

	public static String millisecondsToStringWitNanos(double timeMs) {
		double timeUs = timeMs/1000;
		int nanos = (int) ((timeUs - ((int)timeUs)) * 1000000);
		
		return millisecondsToString((long) timeMs) + "." + String.format("%06d", nanos);
	}

	public static String millisecondsToString(double timeMs) {
		return UtilVerisenseDriver.convertMilliSecondsToFormat((long) timeMs, 
				UtilVerisenseDriver.DATE_FORMAT_NO_MILLIS, 
				UtilVerisenseDriver.TIME_ZONE_STR);
	}

	public static LocalDate toLocalDate(long timeInMilliseconds) {
		return toLocalDate(timeInMilliseconds, DATE_FORMAT_IN_DB, UtilVerisenseDriver.TIME_ZONE_STR);
	}

	public static LocalDate toLocalDate(long timeInMilliseconds, String format, String timezone) {
		LocalDate localDate = null;
		try {
			Date date = new Date(timeInMilliseconds);
			DateFormat formatter = new SimpleDateFormat(format);
			formatter.setTimeZone(TimeZone.getTimeZone(timezone));
			String dateFormatted = formatter.format(date);
			
			DateTimeFormatter dateTimeformatter = DateTimeFormatter.ofPattern(format);
			
			localDate = LocalDate.parse(dateFormatted, dateTimeformatter); 
		} catch (Exception e) {
			System.out.println("Exception in toLocalDate()");
			e.printStackTrace();
		}
		
		return localDate;
	}
	
	public static long getSystemTimeNowUTC() {
		return Instant.now().toEpochMilli();
	}

	public static LocalDate getLocalDateNowUTC() {
		return Instant.now().atZone(TIME_ZONE_ID).toLocalDate();
	}
	
	/** In the form "yyyy/MM/dd HH:mm:ss:SSS"
	 * @param timeString
	 * @return
	 */
	public static long fromTimeStringToMilliseconds(String timeString, String format){
		DateFormat dateFormat = new SimpleDateFormat(format);
		dateFormat.setTimeZone(UtilVerisenseDriver.TIME_ZONE);

		try {
			Date date = dateFormat.parse(timeString);
			return date.getTime();
		} 
		catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	

	//TODO replace with UtilShimmer.getDayOfMonthSuffix by making it public
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

	//TODO replace with UtilShimmer.getMonthString by making it public
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
	
	/**
	 * Converts from milliseconds in Unix time 
	 * 
	 * @param milliSeconds
	 * @param format
	 * @return
	 */
	//TODO move this to UtilShimmer and merge with existing method
	public static String convertMilliSecondsToFormat(long milliSeconds, String format, String timezone) {
		DateFormat dfLocal = new SimpleDateFormat(format);
		dfLocal.setTimeZone(TimeZone.getTimeZone(timezone));
		String timeString = dfLocal.format(new Date(milliSeconds));
		
		if(milliSeconds == 0){
			return "00:00:00";
		}
		else{
			return timeString;
		}
	}

	public static String convertMilliSecondsToCsvHeaderFormat(long milliSeconds) {
		return UtilVerisenseDriver.convertMilliSecondsToFormat(milliSeconds, 
				UtilVerisenseDriver.DATE_FORMAT_VERISENSE_HEADER, 
				UtilVerisenseDriver.TIME_ZONE_STR);
	}

	public static String convertSecondsToHHmmssSSS(double secsOfDataFound) {
		long milliseconds = Math.abs((long)((secsOfDataFound*1000) % 1000));
		return convertSecondsToHHmmss(secsOfDataFound) + String.format(".%03d", milliseconds);
	}

	public static String convertSecondsToHHmmss(double secsOfDataFound) {
		String timeGapSign = secsOfDataFound>=0? "":"-";
		
		double secsOfDataFoundAbs = Math.abs(secsOfDataFound);

		long hours = (long)(secsOfDataFoundAbs / (60 * 60));
		long minutes = (long)((secsOfDataFoundAbs / 60) % 60);
		long seconds = (long)(secsOfDataFoundAbs % 60);
		return timeGapSign + String.format("%02d:%02d:%02d", hours,minutes,seconds);
	}

	public static double[] parseStringArrayToDoubles(String[] dataValueStrings) {
		double[] dataValues = new double[dataValueStrings.length];
		// utilShimmer.consolePrintLn("Length : " + dataValueStrings.length);
		// sdfGgir.setTimeZone(TimeZone.getTimeZone(AsmBinaryFileConstants.TIME_ZONE));		// Set TimeZone to UTC
		for(int i=0;i<dataValueStrings.length;i++){
			String dataValueString = dataValueStrings[i];
			
			//Handle timestamp string ("yyyy-MM-ddTHH:mm:ss+SSSS") in the comparison files
			if(i==0 && dataValueString.contains("T")) {
				try {
					Date date = sdfGgir.parse(dataValueString.replace("T", " "));
					dataValues[i] = date.getTime();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				try { 
					dataValues[i] = Double.parseDouble(dataValueString);
				} catch (NumberFormatException e){
					e.printStackTrace();
				}
			}
		}
		return dataValues;
	}

	public static int countLines(String filePath) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filePath));
		return countLines(is);
	}
	
	public static int countLines(InputStream is) throws IOException {
		try {
			byte[] c = new byte[1024];

			int readChars = is.read(c);
			if (readChars == -1) {
				// bail out if nothing to read
				return 0;
			}

			// make it easy for the optimizer to tune this loop
			int count = 0;
			while (readChars == 1024) {
				for (int i = 0; i < 1024;) {
					if (c[i++] == '\n') {
						++count;
					}
				}
				readChars = is.read(c);
			}

			// count remaining characters
			while (readChars != -1) {
				// System.out.println(readChars);
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
				readChars = is.read(c);
			}

			return count == 0 ? 1 : count;
		} finally {
			is.close();
		}
	}
	
	public static boolean skipHeader(BufferedReader bufferedReader, boolean includeDefaultChannelNamesAndUnits) {
		int y = 0;
		int z = UtilVerisenseDriver.CSV_HEADER_LINES_SEARCH_LIMIT;
		String line = "";
		while(y<z) {
			try {
				line = bufferedReader.readLine();
				if(line==null) {
					return false;
				}
				y++;
				
				// 40 x '-' symbolises the end of the CSV header and the beginning of the channel names, units and then data
				if(UtilVerisenseDriver.isLineCsvHeaderSeparator(line)) {
					if(includeDefaultChannelNamesAndUnits) {
						z = y + 2; //+2 for channel names and units lines
					} else {
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 * @param fileArray -> input file array to filter
	 * @param fileNamePrefix -> prefix of files to keep. If NULL, this parameter is not used.
	 * @param fileNameSuffix -> suffix (including file extension) of files to keep. If NULL, this parameter is not used.
	 * @return filtered array
	 */
	public static File[] filterFileArrayForAnyFileNamePrefixAndSuffixAndSort(File[] fileArray, String fileNamePrefix, String fileNameSuffix, boolean sortAscending) {
		return filterFileArrayForAnyFileNamePrefixAndAnySuffixAndSort(fileArray, fileNamePrefix, new String[] {fileNameSuffix}, sortAscending);
	}

	/**
	 * @param fileArray -> input file array to filter
	 * @param fileNamePrefix -> prefix of files to keep. If NULL, this parameter is not used.
	 * @param fileNameSuffix -> suffix (including file extension) of files to keep. If NULL, this parameter is not used.
	 * @return filtered array
	 */
	public static File[] filterFileArrayForAnyFileNamePrefixAndAnySuffixAndSort(File[] fileArray, String fileNamePrefix, String[] fileNameSuffix, boolean sortAscending) {
		List<File> listOfFiles = new ArrayList<File>();
		
		for(File file:fileArray) {
			String fileName = file.getName();
			if(fileNamePrefix!=null && !fileName.startsWith(fileNamePrefix)) {
				continue;
			}

			if(fileNameSuffix!=null && !doesStringEndWithAny(fileName, fileNameSuffix)) {
				continue;
			}
			
			listOfFiles.add(file);
		}
		
		File[] fileArrayFilt = new File[listOfFiles.size()];
		fileArrayFilt = listOfFiles.toArray(fileArrayFilt);
		if(sortAscending) {
			Arrays.sort(fileArrayFilt);
		} else {
			Arrays.sort(fileArrayFilt, Collections.reverseOrder());
		}
		return fileArrayFilt;
	}

	/**
	 * @param fileArray -> input file array to filter
	 * @param nameContains -> substrings of files names to keep. If NULL, this parameter is not used.
	 * @param fileNameSuffix -> suffix (including file extension) of files to keep. If NULL, this parameter is not used.
	 * @return a new fileArray[] containing matches with nameContains, pathContains and fileSuffice
	 */
	public static File[] filterFileArrayForAllAndRemoveDuplicates(File[] fileArray, String nameContains, String fileNameSuffix) {
		String[][] newArray = null;
		if(nameContains!=null && !nameContains.isEmpty()) {
			newArray = new String[][] {{nameContains}};
		}
		return filterFileArrayForAllAndRemoveDuplicates(fileArray, newArray, null, fileNameSuffix);
	}

	/** This method is particularly used when searching for a matching strings in a set. All strings within any of the 1D arrays must be contained within the name or path. 
	 * 
	 * @param fileArray -> input file array to filter
	 * @param nameContains -> substrings of files names to keep. If NULL, this parameter is not used.
	 * @param pathContains -> substrings of paths to keep. If NULL, this parameter is not used.
	 * @param fileNameSuffix -> suffix (including file extension) of files to keep. If NULL, this parameter is not used.
	 * @return a new fileArray[] containing matches with nameContains, pathContains and fileSuffice
	 */
	public static File[] filterFileArrayForAllAndRemoveDuplicates(File[] fileArray, String[][] nameContains, String[][] pathContains, String fileNameSuffix) {
		return filterFileArrayForAllAndRemoveDuplicates(fileArray, nameContains, pathContains, (fileNameSuffix==null? null:(new String[] {fileNameSuffix})));
	}

	/** This method is particularly used when searching for a matching strings in a set. All strings within any of the 1D arrays must be contained within the name or path. 
	 * 
	 * @param fileArray -> input file array to filter
	 * @param nameContains -> substrings of files names to keep. If NULL, this parameter is not used.
	 * @param pathContains -> substrings of paths to keep. If NULL, this parameter is not used.
	 * @param fileNameSuffixes[] -> suffixes (including file extension) of files to keep. If NULL, this parameter is not used.
	 * @return a new fileArray[] containing matches with nameContains, pathContains and fileNameSuffixes
	 */
	public static File[] filterFileArrayForAllAndRemoveDuplicates(File[] fileArray, String[][] nameContains, String[][] pathContains, String[] fileNameSuffixes) {
		return filterFileArrayForAllAndRemoveDuplicates(fileArray, nameContains, pathContains, fileNameSuffixes, null);
	}
	
	/** This method is particularly used when searching for a matching strings in a set. All strings within any of the 1D arrays must be contained within the name or path. 
	 * 
	 * @param fileArray -> input file array to filter
	 * @param nameContains -> substrings of files names to keep. If NULL, this parameter is not used.
	 * @param pathContains -> substrings of paths to keep. If NULL, this parameter is not used.
	 * @param fileNameSuffixes[] -> suffixes (including file extension) of files to keep. If NULL, this parameter is not used.
	 * @param String[] fileNamePrefixes -> prefixes of filenames to keep. If NULL, this parameter is not used.
	 * @return a new fileArray[] containing matches with nameContains, pathContains, fileNameSuffixes and fileNamePrefixes
	 */
	public static File[] filterFileArrayForAllAndRemoveDuplicates(File[] fileArray, String[][] nameContains, String[][] pathContains, String[] fileNameSuffixes, String[] fileNamePrefixes) {
		List<File> listOfFiles = new ArrayList<File>();
		fileLoop:
		for(File file:fileArray) {
			String fileName = file.getName();
			if(nameContains!=null && !doesStringContainAllOfAnySet(fileName, nameContains)) {
				continue fileLoop;
			}

			if(pathContains!=null && !doesStringContainAllOfAnySet(file.getPath(), pathContains))
			{
				continue fileLoop;
			}
			
			// Remove duplicate files.
			if(file.getName().contains(FILE_FILTER_DUPLICATE)) {
				continue fileLoop;
			}

			if(fileNameSuffixes!=null) {
				for(int i=0;i<fileNameSuffixes.length;i++) {
					if(fileName.endsWith(fileNameSuffixes[i])) {
						break;
					}
					
					// If last entry and still no match, continue to next file
					if(i==fileNameSuffixes.length-1) {
						continue fileLoop;
					}
				}
			}
			
			if(fileNamePrefixes != null) {
				for(int i=0; i<fileNamePrefixes.length; i++) {
					if(fileName.startsWith(fileNamePrefixes[i])) {
						break;
					}
					
					// If last entry and still no match, continue to next file
					if(i==fileNamePrefixes.length-1) {
						continue fileLoop;
					}
				}
			}
			
			listOfFiles.add(file);
		}

		File[] selectedFileArray = listOfFiles.toArray(new File[listOfFiles.size()]);
		return selectedFileArray;
	}
	
	/**
	 * @param fileArray on which to filter out "69" & "70" filename prefixes, and duplicate files 
	 * @return filtered array
	 */
	public static File[] filterOutOfFileArrayDuplicatesAnd69And70Files(File[] fileArray) {
		//1) Filter out duplicates
		File[] files = filterOutOfFileArray(fileArray, new String[] {FILE_FILTER_DUPLICATE}, null);
		//2) Filter out filenames which start with "69" or "70"
		return filterFilenamePrefixesOutOfFileArray(files, new String[] {FILE_FILTER_69, FILE_FILTER_70});
	}
	
	/**
	 * Remove any files with path or naming containing contents of nameContains[] or pathContains[]
	 * 
	 * @param fileArray -> input file array to filter
	 * @param nameContains -> substrings of file names to remove
	 * @param pathContains -> substrings of paths to remove
	 * @return a new fileArray[] containing files that do no not match nameContains or pathContains
	 */
	public static File[] filterOutOfFileArray(File[] fileArray, String[] nameContains, String[] pathContains) {
		List<File> listOfFiles = new ArrayList<File>();
		fileLoop:
		for(File file:fileArray) {
			String fileName = file.getName();
			
			if(nameContains!=null && doesStringContainAny(fileName, nameContains)) {
				continue fileLoop;
			}

			if(pathContains!=null && doesStringContainAny(file.getPath(), pathContains)) {
				continue fileLoop;
			}

			listOfFiles.add(file);
		}

		File[] selectedFileArray = listOfFiles.toArray(new File[listOfFiles.size()]);
		return selectedFileArray;
	}
	
	/**
	 * Remove any files with names that start with prefixContains
	 * 
	 * @param fileArray -> input file array to filter
	 * @param prefixContains -> substrings of file name prefixes to remove
	 * @return a new fileArray[] containing files that do no not match prefixContains
	 */
	public static File[] filterFilenamePrefixesOutOfFileArray(File[] fileArray, String[] prefixContains) {
		List<File> listOfFiles = new ArrayList<File>();
		
		fileLoop:
		for(File file:fileArray) {
			String fileName = file.getName();
						
			if(prefixContains!=null && doesStringStartWithAny(fileName, prefixContains)) {
				continue fileLoop;
			}
			listOfFiles.add(file);
		}

		File[] selectedFileArray = listOfFiles.toArray(new File[listOfFiles.size()]);
		return selectedFileArray;
	}

	public static boolean doesStringContainAllOfAnySet(String strToScan, String[][] setsOfStrToSearch){
		for(String[] setOfStrToSearch:setsOfStrToSearch) {
			if(doesStringContainAll(strToScan, setOfStrToSearch))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean doesStringContainAll(String strToScan, String[] setOfStrToSearch){
		for(String strToSearch:setOfStrToSearch) {
			if(!strToScan.contains(strToSearch)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean doesStringStartWithAny(String strToScan, String[] setOfStrToSearch) {
		for(String strToSearch:setOfStrToSearch) {
			if(strToScan.startsWith(strToSearch)) {
				return true;
			}
		}
		return false;
	}

	public static boolean doesStringEndWithAny(String strToScan, String[] setOfStrToSearch){
		for(String strToSearch:setOfStrToSearch) {
			if(strToScan.endsWith(strToSearch)) {
				return true;
			}
		}
		return false;
	}

	public static boolean doesStringContainAny(String strToScan, String[] setOfStrToSearch){
		for(String strToSearch:setOfStrToSearch) {
			if(strToScan.contains(strToSearch)) {
				return true;
			}
		}
		return false;
	}

	public static File[] filterCalibrationFilesInDir(File directory, final String sensorName, final String range, final String extension){
		File[] listOfFiles = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.endsWith(extension) && fileName.startsWith(sensorName + "_" + range);
			}
		});
		//Sort so that the latest calibration files appear at the start of the array 
		Arrays.sort(listOfFiles, Collections.reverseOrder());
		return listOfFiles;
	}

	public static File[] filterFileDuplicates(File[] listOfFiles) {
		List<File> listToReturn = new ArrayList<File>();
		for(File file:listOfFiles) {
			if(!file.getName().contains(FILE_FILTER_DUPLICATE)) {
				listToReturn.add(file);
			}
		}
		return listToReturn.toArray(new File[listToReturn.size()]);
	}
	
	public static boolean isAnyNullOrEmpty(String[] fields) {
		for(String f:fields) {
			if(isNullOrEmpty(f)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNullOrEmpty(String Key) {
		if(Key == null || Key.isEmpty()) {
			return true;
		}
		return false;
	}

	public static int lsbByteArrayToInt(byte[] byteArray) {
		int retVal = 0;
		for(int i=0;i<byteArray.length;i++) {
			retVal += ((byteArray[i]&0xFF)<<(i*8)); 
		}
		return retVal;
	}
	
	public static boolean isOsWindows() {
		return (System.getProperty("os.name").startsWith("Windows"));
	}
	
	public double getMedian(double[] arrayBuffer) {
		Arrays.sort(arrayBuffer);
		return (arrayBuffer[arrayBuffer.length/2-1] + arrayBuffer[arrayBuffer.length/2])/2;
	}

	public double getAverage(double[] arrayBuffer) {
		double sum = 0;
		for(int i=0;i < arrayBuffer.length;i++) {
			sum+=arrayBuffer[i];
		}
		return sum/arrayBuffer.length;
	}

	public static long getTimeFromDateZoneAdjusted(Date date) {
		return (date.getTime()-(date.getTimezoneOffset()*60000));
	}

	public static long getUnixTimeMsFromVerisenseFileName(File file) {
		String dateTimeInFile = file.getName().substring(0, UtilVerisenseDriver.DATE_FORMAT_FILENAME.length());
		long unixTimeInFileMs = UtilVerisenseDriver.fromTimeStringToMilliseconds(dateTimeInFile, UtilVerisenseDriver.DATE_FORMAT_FILENAME);
		return unixTimeInFileMs;
	}
	
	public static LocalDate getYearMonthDateFromFileName(File file) {
		String lastDate = file.getName().substring(0, 6);
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyMMdd");
		LocalDate endDateToSearch = LocalDate.parse(lastDate, dateFormat);
		return endDateToSearch;
	}
	
	public static void printFileNamesFromArray(File[] fileArray) {
		System.out.println("File names:");
		for(File file:fileArray){
			System.out.println("\t" + file.getName());
		}
	}
	public static String leftPad(String str, int num, String fillChar) {
		return String.format("%1$" + num + "s", str).replace(" ", fillChar);
	}
	
	public static String getDateFormatForVerisenseHeader(boolean isLegacyHeaderDesgin) {
		if(isLegacyHeaderDesgin) {
			return UtilVerisenseDriver.DATE_FORMAT_VERISENSE_HEADER_LEGACY;
		} else {
			return UtilVerisenseDriver.DATE_FORMAT_VERISENSE_HEADER;
		}
	}
	
	public static double calcSamplingRate(double startTimeMs, double endTimeMs, long sampleCount) {
		double samplingRate = Double.NaN;
		if(!Double.isNaN(startTimeMs) && !Double.isNaN(endTimeMs) && sampleCount!=0L && sampleCount!=Long.MIN_VALUE) {
			double tsDiffInS = (endTimeMs - startTimeMs)/1000;
			samplingRate = calcSamplingRate(tsDiffInS, sampleCount);
		}
		return samplingRate;
	}

	public static double calcSamplingRate(double tsDiffInS, long sampleCount) {
		double samplingRate = Double.NaN;
		if(!Double.isNaN(tsDiffInS) && sampleCount!=0L) {
			samplingRate = sampleCount/tsDiffInS;
		}
		return samplingRate;
	}
	
	public static double calculateAverage(List<Double> listOfValues) {
		double sum = 0;
		for (int i = 0; i < listOfValues.size(); i++) {
			sum += listOfValues.get(i);
		}
		return sum / listOfValues.size();
	}

	public static boolean isLineCsvHeaderSeparator(String currentLine) {
		return currentLine.equals(UtilVerisenseDriver.CSV_HEADER_SEPARATOR) ||
				currentLine.equals(UtilVerisenseDriver.CSV_HEADER_SEPARATOR_LEGACY);
	}
	
	public static boolean isHeaderWithCsvHeaderSeparator(String header) {
		return header.contains(UtilVerisenseDriver.CSV_HEADER_SEPARATOR) ||
				header.contains(UtilVerisenseDriver.CSV_HEADER_SEPARATOR_LEGACY);
	}

	//TODO replace the version in UtilShimmer as it doesn't perform well with round numbers
	public static String formatDoubleToNdecimalPlaces(double doubleToFormat, int n){
		
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<n; i++){ //number of digits to right hand side of decimal point
			sb.append("0"); 
		}
		
		DecimalFormat df = new DecimalFormat("#0." + sb.toString()); 
		String formatted = df.format(doubleToFormat);
		
		return formatted;
	}
	
	
	/**
	 * TODO Be better to move this to a util class specific for csv file reading
	 * Retrieves a value from a String (header line in Verisense CSV file) using a prefix and a termination char (;) to isolate the value to return   
	 * e.g. if dataLine = "Data end time: Local = 2021/05/28 14:58:25.391; Unix ms + Local time zone offset = 1622213905391.088; Time since boot ms = 59851.135"
	 * and preceedingStr = "Unix ms + Local time zone offset ="
	 * the @return "1622213905391.088"
	 */
	public static String pickoutParameterFromLine(String dataLine, String preceedingStr) {
		String returnStr = "";
		int indexOfPreceedingStr = dataLine.indexOf(preceedingStr);
		if(indexOfPreceedingStr>=0) {
			// if there's no semi-colon after the preceeding string, assume it's the last parameter in the header line
			int indexOfSemicolon = dataLine.indexOf(";", indexOfPreceedingStr+1);
			int endIndex = indexOfSemicolon>0? indexOfSemicolon:dataLine.length();
			
			returnStr = dataLine.substring(indexOfPreceedingStr + preceedingStr.length(), endIndex);
		}
		return returnStr;
	}
	
	/**
	 * get the date and time in the file name
	 * @return e.g. if file name is '190923_090308_Payload_Metadata_01044.csv' -> this method returns '190923_090308'
	 */
	public static String getDateTimeStrInFileName(File file) {
		return file.getName().substring(0, 13);
	}
	
	/** Dashboard emulator participants (for Demos) are 4 characters long and titled P001-P0XX
	 * @param participantId
	 * @return
	 */
	public static boolean isDashboardEmulatorParticipant(String participantId) {
		return participantId.matches("P[0-9]{3}");
	}

	/** Dashboard emulator sensors (for Demos) are 4 characters long and titled S001-S0XX
	 * @param sensorId
	 * @return
	 */
	public static boolean isDashboardEmulatorSensor(String sensorId) {
		return sensorId.matches("S[0-9]{3}");
	}
	
	public static class FileComparator implements Comparator<File> {
		@Override
		public int compare(File f1, File f2) {
			String fileName1 = extractDateAndTimeInFileName(f1.getName());
			String fileName2 = extractDateAndTimeInFileName(f2.getName());
			return fileName1.compareTo(fileName2);
		}

		private String extractDateAndTimeInFileName(String name) {
			if(name.length() > 13) {
				String dateAndTime = name.substring(0, 13);
				return dateAndTime;
			}
			return name;
		}
	}

}
