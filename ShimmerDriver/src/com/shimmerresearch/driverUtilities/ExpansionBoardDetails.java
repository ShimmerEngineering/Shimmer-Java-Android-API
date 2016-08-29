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
	
	/** * */
	private static final long serialVersionUID = 1104467341565122266L;
	
	public int mExpansionBoardId = HW_ID_SR_CODES.UNKNOWN;
	public int mExpansionBoardRev = HW_ID_SR_CODES.UNKNOWN;
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
		
		if(mExpansionBoardId==ShimmerVerDetails.EXP_BRD_NONE_ID){
			boardName = ShimmerVerDetails.EXP_BRD_NONE;
		}
		else {
			if(ShimmerVerDetails.mMapOfShimmerHardware.containsKey(mExpansionBoardId)){
				boardName = ShimmerVerDetails.mMapOfShimmerHardware.get(mExpansionBoardId);
			}
			else {
				boardName = getBoardVerString();
			}
		}
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
	
	private String getBoardVerString(){
		return ("SR" + mExpansionBoardId + "." + mExpansionBoardRev + "." + mExpansionBoardRevSpecial);
	}
	
	public boolean isExpansionBoardValid(){
		if(!(mExpansionBoardId==0 && mExpansionBoardRev==0 && mExpansionBoardRevSpecial==0)
				&& (mExpansionBoardId!=-1 && mExpansionBoardRev!=-1 && mExpansionBoardRevSpecial!=-1)){
			return true;
		}
		return false;
	}

}
