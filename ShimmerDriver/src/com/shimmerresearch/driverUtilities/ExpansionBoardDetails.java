package com.shimmerresearch.driverUtilities;

import java.io.Serializable;

import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;

/**
 * Holds the Shimmer's Expansion board information as read from the memory chip
 * on the Shimmers expansion board.
 * 
 * @author Mark Nolan
 *
 */
public class ExpansionBoardDetails implements Serializable {
	
	public int mExpansionBoardId = HW_ID_SR_CODES.UNKNOWN;
	public int mExpansionBoardRev = HW_ID_SR_CODES.UNKNOWN;
	public int mExpansionBoardRevSpecial = HW_ID_SR_CODES.UNKNOWN;
	public String mExpansionBoardParsed = UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN;
	public String mExpansionBoardParsedWithVer = UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN;
	public byte[] mExpBoardArray = new byte[]{}; 
	
	public ExpansionBoardDetails(
			int boardID,
			int boardRev,
			int specialRev) {
		parseExpansionBoardDetails(boardID, boardRev, specialRev);
	}
	
	public ExpansionBoardDetails(byte[] mExpBoardArray) {
		if(mExpBoardArray!=null){
			this.mExpBoardArray = mExpBoardArray; 
			int boardID = mExpBoardArray[0] & 0xFF;
			int boardRev = mExpBoardArray[1] & 0xFF;
			int specialRev = mExpBoardArray[2] & 0xFF;
			parseExpansionBoardDetails(boardID, boardRev, specialRev);
		}
	}
	
	public ExpansionBoardDetails() {
		// TODO Auto-generated constructor stub
	}

	private void parseExpansionBoardDetails(int boardID,
											int boardRev,
											int specialRev) {
		String boardName = UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN;
		String boardNameWithVer = UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN;
		String boardVer = "SR" + boardID + "." + boardRev + "." + specialRev;
		
		if(boardID==ShimmerVerDetails.EXP_BRD_NONE_ID){
			boardName = ShimmerVerDetails.EXP_BRD_NONE;
		}
		else {
			if(ShimmerVerDetails.mMapOfShimmerHardware.containsKey(boardID)){
				boardName = ShimmerVerDetails.mMapOfShimmerHardware.get(boardID);
				boardNameWithVer = boardName + " (" + boardVer + ")";
			}
			else {
//				boardName = ShimmerVerDetails.STRING_CONSTANT_FOR_UNKNOWN;
				boardName = boardVer;
			}
		}
			
//		boardNameWithVer = boardName;
//		if((!boardName.equals(ShimmerVerDetails.STRING_CONSTANT_FOR_UNKNOWN))&&(!boardName.equals("None"))){
//			boardNameWithVer += " (SR" + boardID + "." + boardRev + "." + specialRev +")";
//		}
		mExpansionBoardId = boardID;
		mExpansionBoardRev = boardRev;
		mExpansionBoardRevSpecial = specialRev;
		mExpansionBoardParsed = boardName;
		mExpansionBoardParsedWithVer = boardNameWithVer;
	}
}
