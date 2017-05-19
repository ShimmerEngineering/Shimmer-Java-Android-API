package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.HashMap;

import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;

/**
 * Holds the Shimmer's Expansion board information as read from the memory chip
 * on the Shimmers expansion board.
 * 
 * @author Mark Nolan
 *
 */
public class ExpansionBoardDetails implements Serializable {
	
	/** * */
	private static final long serialVersionUID = 1104467341565122266L;
	
	/** Stores the SR number for a specific Shimmer version */
	public int mExpansionBoardId = HW_ID_SR_CODES.UNKNOWN;
	/** Stores the revision of the board */
	public int mExpansionBoardRev = HW_ID_SR_CODES.UNKNOWN;
	/** Stores the special revision number - for custom modifications to standard Shimmers */
	public int mExpansionBoardRevSpecial = HW_ID_SR_CODES.UNKNOWN;
	
	public byte[] mExpBoardArray = new byte[]{}; 
	
	public ExpansionBoardDetails(
			int boardID,
			int boardRev,
			int specialRev) {
		parseExpansionBoardDetails(boardID, boardRev, specialRev);
	}
	
	public ExpansionBoardDetails(byte[] expBoardArray) {
		parseExpansionBoardDetails(expBoardArray);
	}
	
	public ExpansionBoardDetails() {
		// TODO Auto-generated constructor stub
	}

	public void parseExpansionBoardDetails(byte[] expBoardArray) {
		if(expBoardArray!=null){
			this.mExpBoardArray = expBoardArray; 
			int boardID = mExpBoardArray[0] & 0xFF;
			int boardRev = mExpBoardArray[1] & 0xFF;
			int specialRev = mExpBoardArray[2] & 0xFF;
			parseExpansionBoardDetails(boardID, boardRev, specialRev);
		}
	}

	public void parseExpansionBoardDetails(int boardID, int boardRev, int specialRev) {
		mExpansionBoardId = boardID;
		mExpansionBoardRev = boardRev;
		mExpansionBoardRevSpecial = specialRev;
	}

	public String getExpansionBoardParsed() {
		String boardName = UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN;
		
//		if(mExpansionBoardId== || mExpansionBoardId==HW_ID_SR_CODES.UNKNOWN){
//			boardName = ShimmerVerDetails.EXP_BRD_NONE;
//		}
//		else {
			if(ShimmerVerDetails.mMapOfShimmerHardware.containsKey(mExpansionBoardId)){
				boardName = ShimmerVerDetails.mMapOfShimmerHardware.get(mExpansionBoardId);
			}
			else {
				boardName = getBoardVerString();
			}
//		}
		return boardName;
	}

	public String getExpansionBoardParsedWithVer() {
		String boardNameWithVer = UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN;
		String boardVerString = getBoardVerString();
		
		boardNameWithVer = getExpansionBoardParsed();
		if(!boardNameWithVer.equals(boardVerString)){
			boardNameWithVer += " (" + boardVerString + ")";
		}
		return boardNameWithVer;
	}
	
	public String getBoardVerString(){
		return ("SR" + mExpansionBoardId + "." + mExpansionBoardRev + "." + mExpansionBoardRevSpecial);
	}
	
	public boolean isExpansionBoardValid(){
		if(!(mExpansionBoardId==0 && mExpansionBoardRev==0 && mExpansionBoardRevSpecial==0)
				&& (mExpansionBoardId!=HW_ID_SR_CODES.UNKNOWN && mExpansionBoardRev!=HW_ID_SR_CODES.UNKNOWN && mExpansionBoardRevSpecial!=HW_ID_SR_CODES.UNKNOWN)
				&& (mExpansionBoardId!=HW_ID_SR_CODES.LOG_FILE && mExpansionBoardRev!=HW_ID_SR_CODES.LOG_FILE && mExpansionBoardRevSpecial!=HW_ID_SR_CODES.LOG_FILE)){
			return true;
		}
		return false;
	}

	public HashMap<Integer, String> getMapOfByteDescriptions(){
		HashMap<Integer, String> mapOfByteDescriptions = new HashMap<Integer, String>();
		
		mapOfByteDescriptions.put(0, "SR number");
		mapOfByteDescriptions.put(1, "Revision");
		mapOfByteDescriptions.put(2, "Special Revision");
		
		if(mExpansionBoardId==HW_ID_SR_CODES.SHIMMER_GQ_802154_NR || mExpansionBoardId==HW_ID_SR_CODES.SHIMMER_GQ_802154_LR){
			mapOfByteDescriptions.put(5, "MAC_ID_1");
			mapOfByteDescriptions.put(6, "MAC_ID_2");
			mapOfByteDescriptions.put(7, "MAC_ID_3");
			mapOfByteDescriptions.put(8, "MAC_ID_4");
			mapOfByteDescriptions.put(9, "MAC_ID_5");
			mapOfByteDescriptions.put(10, "MAC_ID_6");
		}
		
		return mapOfByteDescriptions;
	}
}
