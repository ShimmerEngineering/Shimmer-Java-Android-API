package com.shimmerresearch.driver;

import java.io.Serializable;

import com.shimmerresearch.driver.ShimmerVerDetails;

/**
 * Holds the Shimmer's Expansion board information as read from the memory chip
 * on the Shimmers expansion board.
 * 
 * @author Mark Nolan
 *
 */
public class ExpansionBoardDetails implements Serializable {
	
	public int mExpBoardId = -1;
	public int mExpBoardRev = -1;
	public int mExpBoardRevSpecial = -1;
	public String mExpBoardParsed = "Unknown";
	public String mExpBoardParsedWithVer = "Unknown";
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
				boardName="Unknown";
			}
		}
			
		boardNameWithVer = boardName;
		if((!boardName.equals("Unknown"))&&(!boardName.equals("None"))){
			boardNameWithVer += " (SR" + boardID + "." + boardRev + "." + specialRev +")";
		}
		mExpBoardId = boardID;
		mExpBoardRev = boardRev;
		mExpBoardRevSpecial = specialRev;
		mExpBoardParsed = boardName;
		mExpBoardParsedWithVer = boardNameWithVer;
	}
}
