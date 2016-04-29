package com.shimmerresearch.comms.serialPortInterface;

import com.shimmerresearch.driver.DeviceException;

public abstract class SerialPortComm implements ByteLevelDataComm {
	//the timeout value for connecting with the port
    public int SERIAL_PORT_TIMEOUT = 500; // was 2000
}
