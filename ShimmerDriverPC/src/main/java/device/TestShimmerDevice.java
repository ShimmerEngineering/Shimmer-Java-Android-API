package device;

import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.shimmer3.communication.ByteCommunication;
import com.shimmerresearch.shimmer3.communication.TestByteRadio;

public class TestShimmerDevice extends ShimmerPC {

	/**
	 * 
	 */
	private static final long serialVersionUID = -100187605073247069L;
	
	protected transient ByteCommunication mByteCommunication=null;

	public void InitializeRadio() {
		TestByteRadio ShimmerRadio = new TestByteRadio();
	}
}