package com.shimmerresearch.driver;

import java.io.Serializable;

import com.shimmerresearch.driver.ShimmerVerDetails;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID_SR_CODES;

/**
 * Holds the Shimmer's Expansion board information as read from the memory chip
 * on the Shimmers expansion board.
 * 
 * @author Mark Nolan
 *
 */
public class ExpansionBoardDetails implements Serializable {
	
	public int mExpBoardId = HW_ID_SR_CODES.UNKNOWN;
	public int mExpBoardRev = HW_ID_SR_CODES.UNKNOWN;
	public int mExpBoardRevSpecial = HW_ID_SR_CODES.UNKNOWN;
	public String mExpBoardParsed = ShimmerVerDetails.STRING_CONSTANT_FOR_UNKNOWN;
	public String mExpBoardParsedWithVer = ShimmerVerDetails.STRING_CONSTANT_FOR_UNKNOWN;
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
		String boardName = "";
		String boardNameWithVer = "";
		
		if(boardID==ShimmerVerDetails.EXP_BRD_NONE_ID){
			boardName = ShimmerVerDetails.EXP_BRD_NONE;
		}
		else {
			if(ShimmerVerDetails.mMapOfShimmerHardware.containsKey(boardID)){
				boardName = ShimmerVerDetails.mMapOfShimmerHardware.get(boardID);
			}
			else {
				boardName = ShimmerVerDetails.STRING_CONSTANT_FOR_UNKNOWN;
			}
		}
			
		boardNameWithVer = boardName;
		if((!boardName.equals(ShimmerVerDetails.STRING_CONSTANT_FOR_UNKNOWN))&&(!boardName.equals("None"))){
			boardNameWithVer += " (SR" + boardID + "." + boardRev + "." + specialRev +")";
		}
		mExpBoardId = boardID;
		mExpBoardRev = boardRev;
		mExpBoardRevSpecial = specialRev;
		mExpBoardParsed = boardName;
		mExpBoardParsedWithVer = boardNameWithVer;
	}
}
