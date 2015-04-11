package com.shimmerresearch.driver;

/**Holds the Shimmer's Expansion board information 
 * 
 * @author Mark Nolan
 *
 */
public class ExpansionBoardDetails {
	
//	public final static int HW_SHIMMER3_EXP_BRD_NONE = 255;
//	public final static int HW_SHIMMER3_EXP_BRD_BR_AMP = 8;
//	public final static int HW_SHIMMER3_EXP_BRD_BR_AMP_UNIFIED = 49;
//	public final static int HW_SHIMMER3_EXP_BRD_GSR = 14;
//	public final static int HW_SHIMMER3_EXP_BRD_GSR_UNIFIED = 48;
//	public final static int HW_SHIMMER3_EXP_BRD_PROTO3_MINI = 36;
//	public final static int HW_SHIMMER3_EXP_BRD_EXG = 37;
//	public final static int HW_SHIMMER3_EXP_BRD_EXG_UNIFIED = 47;
//	public final static int HW_SHIMMER3_EXP_BRD_PROTO3_DELUXE = 38;
//	public final static int HW_SHIMMER3_EXP_BRD_HIGH_G_ACCEL = 44;
//	public final static int HW_SHIMMER3_EXP_BRD_GPS = 46;
	
	public static final class HW_ID_SHIMMER3 {
		public final static int EXP_BRD_NONE = 255;
		public final static int EXP_BRD_BR_AMP = 8;
		public final static int EXP_BRD_BR_AMP_UNIFIED = 49;
		public final static int EXP_BRD_GSR = 14;
		public final static int EXP_BRD_GSR_UNIFIED = 48;
		public final static int EXP_BRD_PROTO3_MINI = 36;
		public final static int EXP_BRD_EXG = 37;
		public final static int EXP_BRD_EXG_UNIFIED = 47;
		public final static int EXP_BRD_PROTO3_DELUXE = 38;
		public final static int EXP_BRD_HIGH_G_ACCEL = 44;
		public final static int EXP_BRD_GPS = 46;
	}

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
		
		switch(boardID){
		case HW_ID_SHIMMER3.EXP_BRD_NONE:
			boardName="None";
			break;
		case HW_ID_SHIMMER3.EXP_BRD_BR_AMP:
		case HW_ID_SHIMMER3.EXP_BRD_BR_AMP_UNIFIED:
			boardName="Bridge Amplifier+";
			break;
		case HW_ID_SHIMMER3.EXP_BRD_GSR:
		case HW_ID_SHIMMER3.EXP_BRD_GSR_UNIFIED:
			boardName="GSR+";
			break;
		case HW_ID_SHIMMER3.EXP_BRD_PROTO3_MINI:
			boardName="PROTO3 Mini";
			break;
		case HW_ID_SHIMMER3.EXP_BRD_EXG:
			boardName="ECG/EMG";
			break;
		case HW_ID_SHIMMER3.EXP_BRD_EXG_UNIFIED:
			boardName="ECG/EMG/Resp";
			break;
		case HW_ID_SHIMMER3.EXP_BRD_PROTO3_DELUXE:
			boardName="PROTO3 Deluxe";
			break;
		case HW_ID_SHIMMER3.EXP_BRD_HIGH_G_ACCEL:
			boardName="High-g Accel";
			break;
		case HW_ID_SHIMMER3.EXP_BRD_GPS:
			boardName="GPS";
			break;
		default:
			boardName="Unknown";
			break;
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
