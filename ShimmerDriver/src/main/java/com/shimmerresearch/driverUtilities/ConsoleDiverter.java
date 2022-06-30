package com.shimmerresearch.driverUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

/**Code copied from UtilShimmerSoftware
 * 
 * @author Mark Nolan
 *
 */
public class ConsoleDiverter {
	
	public static final String PATH_SEP = System.getProperty("file.separator");
	public static final String LOG_FILE_EXT = "_Log.txt";
	
	public static String LOG_FILE_PATH_AND_BASE_NAME = "";
	
	private static PrintStream standardOut = System.out;
	private static PrintStream standardErr = System.err;
	private static FileOutputStream fout = null;
	private static PrintStream consoleAndFilePrintStream;
	
	public static void divertConsoleMessagesToConsoleAndTxtFile(String pathLogFiles) {
		divertConsoleMessagesToConsoleAndTxtFile(pathLogFiles, "", LOG_FILE_EXT, 0);
	}

	public static void divertConsoleMessagesToConsoleAndTxtFile(String pathLogFiles, final String fileNamePrefix, final String fileNameSuffix, int numLogFilesToKeep) {
		
		File logsFolderPath = new File(pathLogFiles);
		if (!logsFolderPath.exists()) {
			logsFolderPath.mkdirs(); // create the folder if it doesn't exist
		}

		if (logsFolderPath.exists()) {
			if(numLogFilesToKeep>0) {
				// maintain max number of log files
				File[] currentLogfiles = logsFolderPath.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.contains(fileNamePrefix) && name.endsWith(fileNameSuffix);
					}
				});
				if (currentLogfiles.length >= numLogFilesToKeep) {
					// Sort files by newest first
					Arrays.sort(currentLogfiles, new Comparator<File>() {
						public int compare(File f1, File f2) {
							return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
						}
					});

					for (int i = numLogFilesToKeep - 1; i < currentLogfiles.length; i++) {
						try {
							Files.delete(currentLogfiles[i].toPath());
						} catch (IOException iOE) {
							iOE.printStackTrace();
						}
					}
				}
			}

//			PrintStream printStream = null;
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
			Calendar cal = Calendar.getInstance();
			try {
				
				LOG_FILE_PATH_AND_BASE_NAME = logsFolderPath + PATH_SEP + (fileNamePrefix.isEmpty()? "":fileNamePrefix+"_") + dateFormat.format(cal.getTime());
						
				fout = new FileOutputStream(LOG_FILE_PATH_AND_BASE_NAME + fileNameSuffix);
				MultiOutputStream multiOut = new MultiOutputStream(standardOut, fout);
				consoleAndFilePrintStream = new PrintStream(multiOut);
				
				setPrintStreamToConsoleAndFile();
				
//				printStream = new PrintStream(new FileOutputStream(logsFolderPath + PATH_SEP + dateFormat.format(cal.getTime()) + LOG_FILE_EXT, true));
//				System.setOut(printStream);
//				System.setErr(printStream);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void divertConsoleMessagesToConsole() {
		System.setOut(standardOut);
		System.setErr(standardErr);
		
		if(fout!=null) {
			try {
				fout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void setPrintStreamToConsoleAndFile(){
		System.setOut(consoleAndFilePrintStream);
		System.setErr(consoleAndFilePrintStream);
	}

	public static void divertConsoleMessagesToConsoleAndTxtFileForUnitTest(String testId) {
		ConsoleDiverter.divertConsoleMessagesToConsoleAndTxtFile("UnitTestConsoleOutput/", testId, ".txt", 5);
	}

}