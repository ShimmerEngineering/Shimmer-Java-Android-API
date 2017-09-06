package com.shimmerresearch.guiUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public abstract class AbstractPlotManager {

	public abstract void setTraceLineStyleAll(PLOT_LINE_STYLE lineStyle);
	
	/** keeps a list of signals to plot */
	//public List<String[]> mListofPropertiestoPlot = new ArrayList<String[]>(); //this is used to identify he signals coming into the filter
	public List<String[]> mListofPropertiestoPlot = Collections.synchronizedList(new ArrayList<String[]>()); //this is used to identify he signals coming into the filter
	
	/** TODO do this based on x-axis data per Signal rather then Shimmer in the keySet */ 
	protected HashMap<String,String[]> mMapofXAxis = new HashMap<String,String[]>();
	/** TODO do this based on x-axis data per Signal rather then Shimmer in the keySet */ 
	protected HashMap<String,Double> mMapofXAxisGeneratedValue = new HashMap<String,Double>();
	
	public List<int[]> mListOfTraceColorsCurrentlyUsed = Collections.synchronizedList(new ArrayList<int[]>());
	
	public static List<int[]> mListofTraceColorsDefault = Collections.synchronizedList(new ArrayList<int[]>());
	{
		String className = this.getClass().getName();
		className ="";
		int[] rgb=new int[3];
		//changed to numeric value so that it`ll work with android
		
		rgb=new int[3];
		// Shimmer Orange
		rgb[0] = 241;
		rgb[1] = 93;
		rgb[2] = 34;
		mListofTraceColorsDefault.add(rgb);

		rgb=new int[3];
		//BROWN
		rgb[0] = 153;
		rgb[1] = 76;
		rgb[2] = 0;
		mListofTraceColorsDefault.add(rgb);
		
		rgb=new int[3];
		//CYAN/AQUA
		rgb[0] = 0;
		rgb[1] = 153;
		rgb[2] = 153;
		mListofTraceColorsDefault.add(rgb);
		
		rgb=new int[3];
		//PURPLE
		rgb[0] = 102;
		rgb[1] = 0;
		rgb[2] = 204;
		mListofTraceColorsDefault.add(rgb);
		
		rgb=new int[3];
		//MAROON
		rgb[0] = 102;
		rgb[1] = 0;
		rgb[2] = 0;
		mListofTraceColorsDefault.add(rgb);
		
		rgb=new int[3];
		//GREEN
		rgb[0] = 0;
		rgb[1] = 153;
		rgb[2] = 76;
		mListofTraceColorsDefault.add(rgb);
		
		rgb=new int[3];
		// Shimmer Grey
		rgb[0] = 119;
		rgb[1] = 120;
		rgb[2] = 124;
		mListofTraceColorsDefault.add(rgb);
		
		rgb=new int[3];
		// Shimmer Blue
		rgb[0] = 0;
		rgb[1] = 129;
		rgb[2] = 198;
		mListofTraceColorsDefault.add(rgb);
	}
	
	public enum PLOT_LINE_STYLE{
		JOINT_LINE("Joint Line"),
		INDIVIDUAL_POINTS("Points"),
		BAR("Bar"), 
		FILL("Fill");
		
		private String mGuiOptionString = "";
		
		private PLOT_LINE_STYLE(String guiOptionString) {
			mGuiOptionString = guiOptionString;
		}
		
		public String getGuiOptionString(){
			return mGuiOptionString;
		}
	}
	public PLOT_LINE_STYLE mSelectedLineStyle = PLOT_LINE_STYLE.JOINT_LINE;

	//generates 
	public AbstractPlotManager(){

	}

	public AbstractPlotManager(List<String[]> propertiestoPlot){
		mListofPropertiestoPlot = propertiestoPlot;
		mListOfTraceColorsCurrentlyUsed = generateRandomColorList(mListofPropertiestoPlot.size());
	}

	public AbstractPlotManager(List<String[]> propertiestoPlot,List<int[]> listOfColors){
		mListofPropertiestoPlot = propertiestoPlot;
		mListOfTraceColorsCurrentlyUsed = listOfColors;
	}

	protected void removeSignal(int index){
		mListofPropertiestoPlot.remove(index);
		if(mListOfTraceColorsCurrentlyUsed.size() > index){
			mListOfTraceColorsCurrentlyUsed.remove(index);
		}
		
	}

	protected void removeAllSignals(){
		mListofPropertiestoPlot.clear();
		mListOfTraceColorsCurrentlyUsed.clear();
	}

	protected void removeCollectionOfSignal(List<String[]> listOfChannelStringArray, List<int[]> colors){
		mListofPropertiestoPlot.removeAll(listOfChannelStringArray);
		mListOfTraceColorsCurrentlyUsed.removeAll(colors);
	}
	
	protected void addSignalGenerateRandomColor(String [] channelStringArray){
		addSignal(channelStringArray);
		mListOfTraceColorsCurrentlyUsed.add(generateRandomColor());
	}
	
	protected void addSignalAndUseFixedColor(String [] channelStringArray, int[] rgb){
		addSignal(channelStringArray);
		mListOfTraceColorsCurrentlyUsed.add(rgb);
	}
	
	/** If all default colors are used a random color will be used
	 * @param channelStringArray
	 */
	protected void addSignalUseDefaultColors(String [] channelStringArray){
		
		addSignal(channelStringArray);
		boolean mFound = false;
		int[] newColorToAdd = null;
		if (mListOfTraceColorsCurrentlyUsed.size() > 0){
			synchronized(mListofTraceColorsDefault){
				Iterator <int[]> entries = mListofTraceColorsDefault.iterator();
				while (entries.hasNext()) {
					int[] rgbdefaultC = entries.next();
					mFound = false;
					for (int[] rgbp : mListOfTraceColorsCurrentlyUsed){
						if (rgbdefaultC[0] == rgbp[0] && rgbdefaultC[1] == rgbp[1] && rgbdefaultC[2] == rgbp[2]){
							mFound = true;
						}
					}
					if (mFound != true){
						newColorToAdd = rgbdefaultC;
					}
				}
			}
		} 
		else {
			newColorToAdd = mListofTraceColorsDefault.get(0);
		}

		if (newColorToAdd != null){
			mListOfTraceColorsCurrentlyUsed.add(newColorToAdd);
		} else {
			mListOfTraceColorsCurrentlyUsed.add(generateRandomColor());
		}
	}

	protected void addSignal(String[] channelStringArray){
		mListofPropertiestoPlot.add(channelStringArray);
	}
	
	protected void addSignalandColor(String[] channelStringArray, int[] color){
		addSignal(channelStringArray);
		mListOfTraceColorsCurrentlyUsed.add(color);
	}
	
	public void addXAxis(String[] channelStringArray){
		String deviceName = channelStringArray[0];
		mMapofXAxis.put(deviceName, channelStringArray);
	}

	/** Checks if the property already exist in the plot manager, there can only be one property
	 * @param channelStringArray A string array containing property/signal details
	 * @return false if the property does not exist in the plot manager
	 */
	public boolean checkIfPropertyExist(String[] channelStringArray){
		synchronized(mListofPropertiestoPlot){
			Iterator <String[]> entries = mListofPropertiestoPlot.iterator();
			while (entries.hasNext()) {
				String[] propertiestoPlot = entries.next();
				boolean test = true;
				int lengthToUse = channelStringArray.length>4? 4:channelStringArray.length;
				for (int p=0; p<lengthToUse; p++){
					if((propertiestoPlot[p]==null) && (channelStringArray[p]==null)){
						test = true;
					}
					else if((propertiestoPlot[p]==null) || (channelStringArray[p]==null)){
						test = false;
					}
					else if (!propertiestoPlot[p].equals(channelStringArray[p])){
						test = false;
					}
				}
				if (test==true){
					return true;
				}
			}
		}
		return false;
	}
	
	public static int[] generateRandomColor(){
		Random rand = new Random();
		int min = 0;
		int max = 255;
		int[] rgb = new int[3];
		rgb[0] = rand.nextInt((max - min) + 1) + min;
		rgb[1] = rand.nextInt((max - min) + 1) + min;
		rgb[2] = rand.nextInt((max - min) + 1) + min;
		return rgb;
	}

	protected static List<int[]> generateRandomColorList(int size){
		List<int[]> listofSignalColors = new ArrayList<int[]>();
		for (int i =0; i<size;i++){
			listofSignalColors.add(generateRandomColor());
		}
		return listofSignalColors;
	}

	public static String joinChannelStringArray(String[] a){
		String js="";
		int lengthToUse = a.length>4? 4:a.length;

//		for (int i=0;i<lengthToUse;i++){
		for (int i=0; i<a.length; i++){
			if (i==0){
				js = a[i];
			} else{
				js = js + " " + a[i];
			}
		}
		return js;
	}

	public List<String[]> getListOfProperties(){
		return mListofPropertiestoPlot;
	}
	
	public List<int []> getListofColors(){
		return mListOfTraceColorsCurrentlyUsed;
	}
	
	public void setPlotLineStyle(PLOT_LINE_STYLE plotLineStyle) {
		mSelectedLineStyle = plotLineStyle;
	}
	
	public PLOT_LINE_STYLE getPlotLineStyle() {
		return mSelectedLineStyle;
	}
	
	public void setTraceLineStyleAll(String selectedLineStyle) {
		for(PLOT_LINE_STYLE plotLineStyle:PLOT_LINE_STYLE.values()){
			if(plotLineStyle.getGuiOptionString().equals(selectedLineStyle)){
				setTraceLineStyleAll(plotLineStyle);
				return;
			}
		}
	}

}
