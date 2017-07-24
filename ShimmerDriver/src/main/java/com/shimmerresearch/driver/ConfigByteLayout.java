package com.shimmerresearch.driver;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;

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
public abstract class ConfigByteLayout implements Serializable {

	private static final long serialVersionUID = -5729543049033754281L;
	
	public byte[] invalidMacId = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
	public byte[] invalidMacId2 = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

	protected ShimmerVerObject mShimmerVerObject = new ShimmerVerObject();
//	protected int mFirmwareIdentifier = -1;
//	protected int mFirmwareVersionMajor = -1;
//	protected int mFirmwareVersionMinor = -1;
//	protected int mFirmwareVersionInternal = -1;
	protected int mInfoMemSize = 512;
	
	public int MSP430_5XX_INFOMEM_D_ADDRESS = 0x001800; 
	public int MSP430_5XX_INFOMEM_C_ADDRESS = 0x001880; 
	public int MSP430_5XX_INFOMEM_B_ADDRESS = 0x001900;
	public int MSP430_5XX_INFOMEM_A_ADDRESS = 0x001980; 
	public int MSP430_5XX_INFOMEM_LAST_ADDRESS = 0x0019FF;
//	public final static int MSP430_5XX_PROGRAM_START_ADDRESS = 0x00FFFE;
	
	public abstract int calculateConfigByteLength(ShimmerVerObject shimmerVerObject);

	
	public int calculateConfigByteLength(){
		return calculateConfigByteLength(mShimmerVerObject);
	}

	/**
	 * Creates an empty byte array for the purposes of generating the
	 * configuration bytes to write to the Shimmer (default all bytes = 0xFF).
	 * 
	 * @return byte array
	 */
	public byte[] createConfigByteArrayEmpty() {
		return createConfigByteArrayEmpty(calculateConfigByteLength());
	}
	
	/**
	 * Creates an empty byte array for the purposes of generating the
	 * configuration bytes to write to the Shimmer.
	 * 
	 * @param size the size of the byte array to create.
	 * @return byte array
	 */
	public static byte[] createConfigByteArrayEmpty(int size) {
		byte[] newArray = new byte[size];
		for(int i=0;i<size;i++){
			newArray[i] = (byte)0x00;
		}
		return newArray;
	}

	/**
	 * Creates an empty byte array for the purposes of generating the
	 * configuration bytes to write to the Shimmer (default all bytes = 0xFF).
	 * 
	 * @param size the size of the byte array to create.
	 * @return byte array
	 */
	public static byte[] createConfigByteArrayDefaultMemoryValues(int size) {
		byte[] newArray = new byte[size];
		for(int i=0;i<size;i++){
			newArray[i] = (byte)0xFF; 
		}
		return newArray;
	}

	public static boolean checkConfigBytesValid(byte[] infoMemContents){
		// Check first 6 bytes of InfoMem for 0xFF to determine if contents are valid 
		byte[] comparisonBuffer = new byte[]{-1,-1,-1,-1,-1,-1};
		byte[] detectionBuffer = new byte[comparisonBuffer.length];
		System.arraycopy(infoMemContents, 0, detectionBuffer, 0, detectionBuffer.length);
		if(Arrays.equals(comparisonBuffer, detectionBuffer)) {
			return false;
		}
		return true;
	}
	
	public boolean isDifferent(int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionInternal) {
		if((mShimmerVerObject.mFirmwareIdentifier!=firmwareIdentifier)
				||(mShimmerVerObject.mFirmwareVersionMajor!=firmwareVersionMajor)
				||(mShimmerVerObject.mFirmwareVersionMinor!=firmwareVersionMinor)
				||(mShimmerVerObject.mFirmwareVersionInternal!=firmwareVersionInternal)){
			return true;
		}
		return false;
	}

	protected boolean compareVersions(int compFwIdent, int compMajor, int compMinor, int compInternal){
//		return UtilShimmer.compareVersions(mShimmerVerObject.mFirmwareIdentifier,mShimmerVerObject.mFirmwareVersionMajor,mShimmerVerObject.mFirmwareVersionMinor,mShimmerVerObject.mFirmwareVersionInternal,
//				compFwIdent,compMajor,compMinor,compInternal);
		
		return mShimmerVerObject.compareVersions(compFwIdent, compMajor, compMinor, compInternal);
	}


	public HashMap<Integer, String> getMapOfByteDescriptions() {
		// TODO Auto-generated method stub
		return null;
	}

}
