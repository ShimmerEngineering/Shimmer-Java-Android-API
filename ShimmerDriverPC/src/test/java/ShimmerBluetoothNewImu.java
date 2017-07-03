

import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.pcDriver.ShimmerPC;

public class ShimmerBluetoothNewImu {
	
	public static void main(String[] args) {
		
		ShimmerPC shimmerBluetooth = new ShimmerPC("TestShimmer", false);
		
		shimmerBluetooth.setHardwareVersionAndCreateSensorMaps(HW_ID.SHIMMER_3);
		shimmerBluetooth.setExpansionBoardDetailsAndCreateSensorMap(new ExpansionBoardDetails(HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED, 3, 0));
		
	}
	

}
