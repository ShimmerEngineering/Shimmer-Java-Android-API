package com.shimmerresearch.driverUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.UtilShimmer.ENUM_FILE_DELIMITERS;
import com.shimmerresearch.driverUtilities.UtilShimmer.ENUM_FILE_FORMAT;

public class ParserStreamedDataToCSV {

	private String fileDelimiter = ENUM_FILE_DELIMITERS.TAB.delimiter;
	private String fileExtension = ENUM_FILE_FORMAT.DAT.fileExtension;
	public final static String PATH_SEP = System.getProperty("file.separator");

	private BufferedWriter bufferedWriter;
	
	private boolean isFileDelimiterDescriptorInFile = false;
	private boolean isFileHeaderWritten;
	
	private String filePath;
	private String fileName;
	
	private List<String> listOfChannelNamesToWriteToCSV = new ArrayList<String>();
	
	public ParserStreamedDataToCSV(String filePath, 
			String fileName, 
			List<String> listOfChannelNamesToWriteToCSV){
		
		this.filePath = filePath;
		this.fileName = fileName;
		this.listOfChannelNamesToWriteToCSV = listOfChannelNamesToWriteToCSV;
		
		this.isFileHeaderWritten = false;
		this.bufferedWriter = null;
	}

	public void setFileSeparatorAndExtension(String fileSeparator, String fileExtension){
		setFileSeparator(fileSeparator);
		setFileExtension(fileExtension);
	}
	
	private void setFileSeparator(String fileSeparator){
		this.fileDelimiter = fileSeparator;
	}
	
	public String getFileSeparator() {
		return this.fileDelimiter;
	}
	
	private void setFileExtension(String fileExtension){
		this.fileExtension = fileExtension;
	}
	
	public String getFileExtension() {
		return this.fileExtension;
	}
	
	public String getFilePath() {
		return this.filePath;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public void setFileDelimiterDescriptorInFile(boolean isFileDelimiterDescriptorInFile){
		this.isFileDelimiterDescriptorInFile = isFileDelimiterDescriptorInFile;
	}
	
	public void parseDataToCSV(ObjectCluster ojc) {
		createCSVAndWriteHeader(ojc);
		writeDataToCSVFile(ojc);
	}

	public void parseDataToCSV(ObjectCluster[] ojcArray) {
		for(ObjectCluster ojc : ojcArray){
			createCSVAndWriteHeader(ojc);
			writeDataToCSVFile(ojc);
		}
	}
	
	private void createCSVAndWriteHeader(ObjectCluster ojc){
		if(!isFileHeaderWritten){
			createCSVFile();
			prepareAndWriteHeaderToCSVFile(ojc);
		}
	}

	public void createCSVFile() {
		String fullPathFile = filePath + PATH_SEP + fileName + fileExtension;
		File file = UtilShimmer.createFileAndDeleteIfExists(fullPathFile);
		
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void prepareAndWriteHeaderToCSVFile(ObjectCluster ojc) {
		String channelsHeader = "";
		String unitsHeader = "";
		
		List<String> allChannelNames = ojc.getChannelNamesByInsertionOrder();
		if(isListOfChannelNamesToWriteToCSVInvalid()){
			for (String channelNameToSave : allChannelNames) {
				channelsHeader += formatHeaderChannelName(ojc.getShimmerName() + "_" + channelNameToSave);
				unitsHeader += formatHeaderChannelUnits(channelNameToSave, ojc);
			}			
		}
		else {
			for (String channelNameToSave : listOfChannelNamesToWriteToCSV) {
				if(allChannelNames.contains(channelNameToSave)){
					channelsHeader += formatHeaderChannelName(ojc.getShimmerName() + "_" + channelNameToSave);
					unitsHeader += formatHeaderChannelUnits(channelNameToSave, ojc);
				}
			}		
		}
		
		writeDoubleLineHeaderToCSVFile(channelsHeader, unitsHeader);
	}

	public void writeDoubleLineHeaderToCSVFile(String channelsHeader, String unitsHeader) {
		try {
			writeSingleLineHeaderToCSVFile(channelsHeader);
			bufferedWriter.write(unitsHeader);
			bufferedWriter.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		isFileHeaderWritten = true;
	}
	
	public void writeSingleLineHeaderToCSVFile(String channelsHeader) {
		try {
			if(isFileDelimiterDescriptorInFile){
				bufferedWriter.write("\"sep=" + fileDelimiter + "\"");
				bufferedWriter.newLine();
			}
			bufferedWriter.write(channelsHeader);
			bufferedWriter.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		isFileHeaderWritten = true;
	}
	
	private String formatHeaderChannelName(String channelName) {
		return channelName + fileDelimiter;
	}

	private String formatHeaderChannelUnits(String channelNameToSave, ObjectCluster ojc) {
		FormatCluster formatCluster = ojc.getLastFormatCluster(channelNameToSave, CHANNEL_TYPE.CAL.toString());
		if(formatCluster!=null){
			return String.valueOf(formatCluster.mUnits) + fileDelimiter;
		} else {
			return " " + fileDelimiter;
		}
	}

	private void writeDataToCSVFile(ObjectCluster ojc){
		String sampleToWrite = "";
		
		if(isListOfChannelNamesToWriteToCSVInvalid()){
			List<String> allChannelNames = ojc.getChannelNamesByInsertionOrder();
			for(String channelName : allChannelNames){
				double value = ojc.getFormatClusterValue(channelName, CHANNEL_TYPE.CAL.toString());
				sampleToWrite = sampleToWrite + String.valueOf(value) + fileDelimiter;
			}
		} 
		else {
			for(String channelName : listOfChannelNamesToWriteToCSV){
				double value = ojc.getFormatClusterValue(channelName, CHANNEL_TYPE.CAL.toString());
				sampleToWrite = sampleToWrite + String.valueOf(value) + fileDelimiter;
			}
		}
		
		writeSample(sampleToWrite);
	}
	
	public void writeSample(String sampleToWrite) {
		try {
			bufferedWriter.write(sampleToWrite);
			bufferedWriter.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stopWritingDataToCSVFile(){
		try {
			if(bufferedWriter != null){
				bufferedWriter.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	private boolean isChannelCompatibleWithWriteToFile(String channelName){
//		return isListOfChannelNamesToWriteToCSVInvalid() || 
//				listOfChannelNamesToWriteToCSV.contains(channelName);
//	}
	
	private boolean isListOfChannelNamesToWriteToCSVInvalid(){
		return listOfChannelNamesToWriteToCSV == null || 
				listOfChannelNamesToWriteToCSV.isEmpty(); 
	}
	
//	private int getIndexOfChannelName(String channelName){
//		for(int i=0; i<listOfChannelNamesToWriteToCSV.size(); i++){
//			if(channelName.equals(listOfChannelNamesToWriteToCSV.get(i))){
//				return i;
//			}
//		}
//		return -1;
//	}
	
}
