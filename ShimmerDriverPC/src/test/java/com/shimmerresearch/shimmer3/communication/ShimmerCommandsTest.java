package com.shimmerresearch.shimmer3.communication;

import org.junit.Test;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.pcDriver.ShimmerPC;

public class ShimmerCommandsTest {
    String uuid = "00000000-0000-0000-0000-4b78d9e37992";
    ShimmerPC shimmer = new ShimmerPC(uuid);
    TestByteRadio testByteRadio = new TestByteRadio();

    @Test
    public void Setup() {
        testByteRadio.Initialize(shimmer);
    }

    @Test
    public void Connect() {
    	Setup();
        boolean result = testByteRadio.Connect();
        if (result && shimmer.getBluetoothRadioState() == BT_STATE.CONNECTED) {
            assert(true);
        } else {
            assert(false);
        }
    }

    @Test
    public void Disconnect() {
    	Setup();
        boolean result = testByteRadio.Connect();
        if (result) {
            testByteRadio.Disconnect();
            if (result && shimmer.getBluetoothRadioState() == BT_STATE.DISCONNECTED) {
                assert(true);
            } else {
                assert(false);
            }
        }
    }

    @Test
    public void ReadFirmwareVersion() {
    	Setup();
    	testByteRadio.WriteFirmwareVersion();
        boolean result = testByteRadio.Connect();
        if (result && shimmer.getBluetoothRadioState() == BT_STATE.CONNECTED) {
            if (shimmer.getFirmwareVersionMajor() == 0 && shimmer.getFirmwareVersionMinor() == 16) {
                assert(true);
            }
            else {
                assert(false);
            }
        }
    }

    @Test
    public void ReadHardwareVersion() {
    	Setup();
    	testByteRadio.WriteHardwareVersion();
        boolean result = testByteRadio.Connect();
        if (result && shimmer.getBluetoothRadioState() == BT_STATE.CONNECTED) {
            if (shimmer.getHardwareVersion() == 3) {
                assert(true);
            }
            else {
                assert(false);
            }
        }
    }
}