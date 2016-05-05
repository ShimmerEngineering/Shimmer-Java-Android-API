package com.shimmerresearch.pcDriver;

import jssc.SerialPort;

import com.shimmerresearch.bluetooth.ShimmerRadioProtocol;
import com.shimmerresearch.comms.radioProtocol.LiteProtocol;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Shimmer4;
import com.shimmerresearch.pcserialport.ShimmerSerialPortJssc;

public class Shimmer4PC extends Shimmer4{
	
	/** * */
	private static final long serialVersionUID = -4688621228636286260L;

	public Shimmer4PC() {
		// TODO Auto-generated constructor stub
	}

	public Shimmer4PC(String dockId, int slotNumber, String macId, COMMUNICATION_TYPE connectionType) {
		super(dockId, slotNumber, macId, connectionType);
	}

	public void initialize(){
		mShimmerRadio = new ShimmerRadioProtocol(new ShimmerSerialPortJssc(this.getMacIdFromUart(),this.getMacId(),SerialPort.BAUDRATE_115200), new LiteProtocol());
		super.initialize();
	}


}