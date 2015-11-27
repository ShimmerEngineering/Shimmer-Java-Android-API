package com.shimmerresearch.driver;

import java.io.Serializable;

import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;

/**
 * Hold the Shimmer microcontrollers information memory layout. This region of
 * the the microcontrollers RAM can be used to configure all properties of the
 * Shimmer when configured through a docking station using Consensys. Variables
 * stored in this class are based on firmware header files for mapping which
 * bits in each information memory byte represents various configurable settings
 * on the Shimmer.
 * 
 * @author Mark Nolan
 *
 */
public abstract class InfoMemLayout implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5729543049033754281L;
	
	public byte[] invalidMacId = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
	public byte[] invalidMacId2 = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

	int mFirmwareIdentifier = -1;
	int mFirmwareVersionMajor = -1;
	int mFirmwareVersionMinor = -1;
	int mFirmwareVersionInternal = -1;
	int mInfoMemSize = 512;
	
	public int MSP430_5XX_INFOMEM_D_ADDRESS = 0x001800; 
	public int MSP430_5XX_INFOMEM_C_ADDRESS = 0x001880; 
	public int MSP430_5XX_INFOMEM_B_ADDRESS = 0x001900;
	public int MSP430_5XX_INFOMEM_A_ADDRESS = 0x001980; 
	public int MSP430_5XX_INFOMEM_LAST_ADDRESS = 0x0019FF;
//	public final static int MSP430_5XX_PROGRAM_START_ADDRESS = 0x00FFFE;
	
	
	public abstract int calculateInfoMemByteLength(int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionRelease);
//	public static int calculateInfoMemByteLength(int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionRelease) {
//		
//		//TODO: should add full FW version checking here to support different size InfoMems in the future
////		if(Util.compareVersions(firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionRelease,
////				FW_ID.SDLOG, 0, 10, 1)) {
////			return 512;
////		}
//		
////		if(firmwareIdentifier == FW_ID.SDLOG) {
////			return 384;
////		}
////		else if(firmwareIdentifier == FW_ID.BTSTREAM) {
////			return 128;
////		}
////		else if(firmwareIdentifier == FW_ID.LOGANDSTREAM) {
////			return 384;
////		}
////		else if(firmwareIdentifier == FW_ID.GQ_GSR) {
////			return 128;
////		}
////		else {
////			return 512; 
////		}
//		
//		return 384;
//	}
	
	public int calculateInfoMemByteLength(){
		return calculateInfoMemByteLength(mFirmwareIdentifier, mFirmwareVersionMajor, mFirmwareVersionMinor, mFirmwareVersionInternal);
	}

	public boolean isDifferent(int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionInternal) {
		if((mFirmwareIdentifier!=firmwareIdentifier)
				||(mFirmwareVersionMajor!=firmwareVersionMajor)
				||(mFirmwareVersionMinor!=firmwareVersionMinor)
				||(mFirmwareVersionInternal!=firmwareVersionInternal)){
			return true;
		}
		return false;
	}

}
