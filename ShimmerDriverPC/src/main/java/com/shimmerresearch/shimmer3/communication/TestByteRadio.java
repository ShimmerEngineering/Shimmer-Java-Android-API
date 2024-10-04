package com.shimmerresearch.shimmer3.communication;

import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.pcDriver.ShimmerPC;

public class TestByteRadio extends ShimmerPC {
    /**
     *
     */
    private static final long serialVersionUID = 6214064195429194840L;

    public byte[] ReadFirmwareVersion = new byte[]{0x2F};
    public byte[] ReadShimmerVersion = new byte[]{0x25};

    ShimmerPC mShimmer;

    public void Initialize(ShimmerPC shimmer) {
        mShimmer = shimmer;
    }

    public boolean Connect() {
        mShimmer.setBluetoothRadioState(BT_STATE.CONNECTED);
        return true;
    }

    public boolean Disconnect() {
        mShimmer.setBluetoothRadioState(BT_STATE.DISCONNECTED);
        return true;
    }
    
    public boolean WriteBytes(byte[] bytes) {
		if (bytes[0] == ReadFirmwareVersion[0]) {
			byte[] bufferInquiry = {3, 0, 0, 0, 16, 9};

			if(bufferInquiry!=null){
				int firmwareIdentifier=(int)((bufferInquiry[1]&0xFF)<<8)+(int)(bufferInquiry[0]&0xFF);
				int firmwareVersionMajor = (int)((bufferInquiry[3]&0xFF)<<8)+(int)(bufferInquiry[2]&0xFF);
				int firmwareVersionMinor = ((int)((bufferInquiry[4]&0xFF)));
				int firmwareVersionInternal=(int)(bufferInquiry[5]&0xFF);
				ShimmerVerObject shimmerVerObject = new ShimmerVerObject(getHardwareVersion(), firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
				mShimmer.setShimmerVersionObjectAndCreateSensorMap(shimmerVerObject);
			}
		}
		else if (bytes[0] == ReadShimmerVersion[0]) {
			byte[] bufferShimmerVersion = {3};
			
			if(bufferShimmerVersion!=null){
				mShimmer.setHardwareVersion((int)bufferShimmerVersion[0]);
			}
		}
		return true;
    }

    public void WriteFirmwareVersion() {
    	WriteBytes(ReadFirmwareVersion);
    }

    public void WriteHardwareVersion() {
    	WriteBytes(ReadShimmerVersion);
    }
}


