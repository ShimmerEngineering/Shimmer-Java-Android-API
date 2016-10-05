package com.shimmerresearch.comms.wiredProtocol;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**Holds HW information of a connected SmartDock (used by DeviceInfo)
 * @author Mark Nolan
 *
 */
public class SmartDockActiveSlotDetails {
	
	public final static int CONNECTION_TYPE_DISCONNECTED = 0;
	public final static int CONNECTION_TYPE_WITH_SD_CARD = 1;
	public final static int CONNECTION_TYPE_WITHOUT_SD_CARD = 2;
    public static final Map<Integer, String> mMapOfConnectionTypes;
    static {
        Map<Integer, String> aMap = new TreeMap<Integer,String>();
        aMap.put(CONNECTION_TYPE_DISCONNECTED, "Disconnected");
        aMap.put(CONNECTION_TYPE_WITH_SD_CARD, "With SD access");
        aMap.put(CONNECTION_TYPE_WITHOUT_SD_CARD, "Without SD access");
        mMapOfConnectionTypes = Collections.unmodifiableMap(aMap);
    }

	public int mSlot = -1;
	public int mConnectionType = CONNECTION_TYPE_DISCONNECTED;
}
