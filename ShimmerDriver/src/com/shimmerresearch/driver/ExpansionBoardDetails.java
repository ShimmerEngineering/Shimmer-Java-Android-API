package com.shimmerresearch.driver;

import com.shimmerresearch.driver.ShimmerHwFw;

/**Holds the Shimmer's Expansion board information 
 * 
 * @author Mark Nolan
 *
 */
public class ExpansionBoardDetails {
	
	public int mExpBoardId = -1;
	public int mExpBoardRev = -1;
	public int mExpBoardSpecialRev = -1;
	public String mExpBoardParsed = "";
	public String mExpBoardParsedWithVer = "";
	
	public ExpansionBoardDetails(int boardID,
											int boardRev,
											int specialRev) {
		parseExpansionBoardDetails(boardID, boardRev, specialRev);
	}
	
	public ExpansionBoardDetails(byte[] mExpBoardArray) {
		if(mExpBoardArray!=null){
			int boardID = mExpBoardArray[0] & 0xFF;
			int boardRev = mExpBoardArray[1] & 0xFF;
			int specialRev = mExpBoardArray[2] & 0xFF;
			parseExpansionBoardDetails(boardID, boardRev, specialRev);
		}
	}
	
	private void parseExpansionBoardDetails(int boardID,
											int boardRev,
											int specialRev) {
		String boardName = "";
		String boardNameWithVer = "";
		
		if(boardID==ShimmerHwFw.EXP_BRD_NONE_ID){
			boardName = ShimmerHwFw.EXP_BRD_NONE;
		}
		else {
			if(ShimmerHwFw.mMapOfShimmmerHardware.containsKey(boardID)){
				boardName = ShimmerHwFw.mMapOfShimmmerHardware.get(boardID);
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
		mExpBoardSpecialRev = specialRev;
		mExpBoardParsed = boardName;
		mExpBoardParsedWithVer = boardNameWithVer;
	}
}
