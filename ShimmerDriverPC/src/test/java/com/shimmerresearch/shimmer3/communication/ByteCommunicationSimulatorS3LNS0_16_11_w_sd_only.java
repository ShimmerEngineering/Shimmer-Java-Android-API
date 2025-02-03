package com.shimmerresearch.shimmer3.communication;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class ByteCommunicationSimulatorS3LNS0_16_11_w_sd_only extends ByteCommunicationSimulatorS3{

	public ByteCommunicationSimulatorS3LNS0_16_11_w_sd_only(String address) {
		super(address);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void txShimmerVersion() {
		mBuffer.add((byte) 0xff);
		mBuffer.add((byte) 0x25);
		mBuffer.add((byte) 0x03);
	}
	
	@Override 
	protected void txFirmwareVersion() {
		mBuffer.add((byte) 0xff);
		mBuffer.add((byte) 0x2f);
		mBuffer.add((byte) 0x03);
		mBuffer.add((byte) 0x00);
		mBuffer.add((byte) 0x00);
		mBuffer.add((byte) 0x00);
		mBuffer.add((byte) 0x10);
		mBuffer.add((byte) 0x0B);
	}
	
	@Override
	protected void txInfoMem(byte[] buffer) {

		if (buffer[1]==(byte)0x80 && buffer[2]==(byte)0x00 && buffer[3]==(byte)0x00) //0x8E 0x80 0x00 0x00
		{
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x8D);
			mBuffer.add((byte) 0x80);
			byte[] bytes = UtilShimmer.hexStringToByteArray("80020180000001FF010800801000000000000201008010000000000002010900000008CD08CD08CD005C005C005C009C009C000000009C000000000000199619961996009C009C000000009C000000000000029B029B029B009C0064000000009C000000000000068706870687009C0064000000009C00000000000000000000");
			for (byte b:bytes) {
				mBuffer.add(b);
			}
			mBuffer.add((byte) 0x0F);
		}
		else if (buffer[1]==(byte)0x80 && buffer[2]==(byte)0x80 && buffer[3]==(byte)0x00) //0x8E 0x80 0x80 0x00
		{
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x8D);
			mBuffer.add((byte) 0x80);
			byte[] bytes = UtilShimmer.hexStringToByteArray("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005368696D6D65725F3345333673796E637465737437FFFFFF6729A22D010139003600010000E8EB1B713E360100000000000000000000000000000000000000000000000000");
			for (byte b:bytes) {
				mBuffer.add(b);
			}

			mBuffer.add((byte) 0xea);
		}	else if (buffer[1]==(byte)0x80 && buffer[2]==(byte)0x00 && buffer[3]==(byte)0x01) //[0x8E 0x80 0x00 0x01]
		{
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x8D);
			mBuffer.add((byte) 0x80);
			byte[] bytes = UtilShimmer.hexStringToByteArray("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF0000");
			for (byte b:bytes) {
				mBuffer.add(b);
			}

			mBuffer.add((byte) 0x84);
		}	
	}
	
}
