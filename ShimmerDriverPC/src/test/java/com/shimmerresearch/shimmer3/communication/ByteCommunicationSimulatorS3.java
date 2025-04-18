package com.shimmerresearch.shimmer3.communication;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class ByteCommunicationSimulatorS3 implements ByteCommunication{
	
	public boolean isGetBmp280CalibrationCoefficientsCommand = false;
	public boolean isGetPressureCalibrationCoefficientsCommand = false;
	public boolean mIsNewBMPSupported;

	public ByteCommunicationSimulatorS3(String address) {
		// TODO Auto-generated constructor stub
	}
	
	public void setIsNewBMPSupported(boolean isNewBMPSupported) {
		mIsNewBMPSupported = isNewBMPSupported;
	}

	@Override
	public int getInputBufferBytesCount() throws SerialPortException {
		// TODO Auto-generated method stub
		return mBuffer.size();
	}

	@Override
	public boolean isOpened() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean closePort() throws SerialPortException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean openPort() throws SerialPortException {
		// TODO Auto-generated method stub
		return true;
	}

	protected void txInfoMem(byte[] buffer) {

		if (buffer[1]==(byte)0x80 && buffer[2]==(byte)0x00 && buffer[3]==(byte)0x00) //0x8E 0x80 0x00 0x00
		{
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x8D);
			mBuffer.add((byte) 0x80);
			byte[] bytes = UtilShimmer.hexStringToByteArray("800201E010004D9B0E08008010000000000002010080100000000000020109000000083C081F0825005300530054019C009C0100FEFF9C0000000000000CD00CD00CD0009C009C000000009C002AFBEDFC21010700E500FD009C0064000000009C00000000000001A201A201A2009C0064000000009C0000000000FFFFFFFFFF");
			for (byte b:bytes) {
				mBuffer.add(b);
			}
			mBuffer.add((byte) 0x8c);
		}
		else if (buffer[1]==(byte)0x80 && buffer[2]==(byte)0x80 && buffer[3]==(byte)0x00) //0x8E 0x80 0x80 0x00
		{
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x8D);
			mBuffer.add((byte) 0x80);
			byte[] bytes = UtilShimmer.hexStringToByteArray("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005368696D6D65725F33453336574354455354FFFFFFFFFFFF670773A70000B9003600000000E8EB1B713E360000000000000000000000000000000000000000000000000000");
			for (byte b:bytes) {
				mBuffer.add(b);
			}

			mBuffer.add((byte) 0x32);
		}	else if (buffer[1]==(byte)0x80 && buffer[2]==(byte)0x00 && buffer[3]==(byte)0x01) //[0x8E 0x80 0x00 0x01]
		{
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x8D);
			mBuffer.add((byte) 0x80);
			byte[] bytes = UtilShimmer.hexStringToByteArray("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
			for (byte b:bytes) {
				mBuffer.add(b);
			}

			mBuffer.add((byte) 0x9B);
		}	
	}
	
	protected void txShimmerVersion() {
		mBuffer.add((byte) 0xff);
		mBuffer.add((byte) 0x25);
		mBuffer.add((byte) 0x03);
	}
	
	protected void streaming() {
		//not implemented for connect test please see ByteCommunicationS3_streaming.java
	}
	
	protected void inquiryResponse() {
		mBuffer.add((byte) 0xff);
		mBuffer.add((byte) 0x02);
		byte[] bytes = UtilShimmer.hexStringToByteArray("800202FF01080001");
		for (byte b:bytes) {
			mBuffer.add(b);
		}
		mBuffer.add((byte) 0x86);
	}
	
	protected void txFirmwareVersion() {
		mBuffer.add((byte) 0xff);
		mBuffer.add((byte) 0x2f);
		mBuffer.add((byte) 0x03);
		mBuffer.add((byte) 0x00);
		mBuffer.add((byte) 0x00);
		mBuffer.add((byte) 0x00);
		mBuffer.add((byte) 0x10);
		
		if(mIsNewBMPSupported) {
			mBuffer.add((byte) 0x09);
		}else {
			mBuffer.add((byte) 0x00);
		}
	}
	
	@Override
	public byte[] readBytes(int byteCount, int timeout) throws SerialPortTimeoutException, SerialPortException {
		if (byteCount <= 0) {
            throw new IllegalArgumentException("Number of bytes to read must be positive.");
        }
        byte[] result = new byte[byteCount];

        for (int i = 0; i < byteCount; i++) {
            try {
				result[i] = mBuffer.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Blocks if the buffer is empty
        }

        //System.out.println("Read " + byteCount + " bytes from buffer.");
        return result;
	}
	BlockingQueue<Byte> mBuffer = new ArrayBlockingQueue<>(1000); // Fixed size 1000
	@Override
	public boolean writeBytes(byte[] buffer) throws SerialPortException {
		// TODO Auto-generated method stub
		
		if(buffer[0]==ShimmerObject.GET_SHIMMER_VERSION_COMMAND_NEW) {
			System.out.println(UtilShimmer.bytesToHexString(buffer));
			txShimmerVersion();
		}  else if(buffer[0]==ShimmerObject.GET_VBATT_COMMAND) {
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x8A);
			mBuffer.add((byte) 0x94);
			byte[] bytes = UtilShimmer.hexStringToByteArray("240B80");
			for (byte b:bytes) {
				mBuffer.add(b);
			}
			mBuffer.add((byte) 0x61);
		} else if(buffer[0]==ShimmerObject.GET_SAMPLING_RATE_COMMAND) {
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x04);
			mBuffer.add((byte) 0x80);
			mBuffer.add((byte) 0x02);
		} else if(buffer[0]==ShimmerObject.SET_CRC_COMMAND) {
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x04);
			mBuffer.add((byte) 0xf4);
		} else if(buffer[0]==ShimmerObject.GET_FW_VERSION_COMMAND) {
			txFirmwareVersion();
		} else if(buffer[0]==ShimmerObject.GET_DAUGHTER_CARD_ID_COMMAND){
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x65);
			mBuffer.add((byte) 0x03);
			mBuffer.add((byte) 0x30);
			mBuffer.add((byte) 0x04);
			mBuffer.add((byte) 0x02);
		} else if (buffer[0]==ShimmerObject.GET_INFOMEM_COMMAND){	
			txInfoMem(buffer);
		} else if(buffer[0]==ShimmerObject.GET_BMP280_CALIBRATION_COEFFICIENTS_COMMAND) {
			isGetBmp280CalibrationCoefficientsCommand = true;
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x9f);
			byte[] bytes = UtilShimmer.hexStringToByteArray("7A6A0D6632007F9016D7D00BBC1B2AFFF9FF8C3CF8C67017");
			for (byte b:bytes) {
				mBuffer.add(b);
			}
			mBuffer.add((byte) 0xDF);
		}else if(buffer[0]==ShimmerObject.GET_PRESSURE_CALIBRATION_COEFFICIENTS_COMMAND) {
			isGetPressureCalibrationCoefficientsCommand = true;
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0xa6);
			byte[] bytes = UtilShimmer.hexStringToByteArray("19011D6BBA643200859289D6D00BC918CBFFF9FF7B1A1FEE4DFC");
			for (byte b:bytes) {
				mBuffer.add(b);
			}
			mBuffer.add((byte) 0xDF);			
		} else if(buffer[0]==ShimmerObject.GET_BLINK_LED) {
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x31);
			mBuffer.add((byte) 0x01);
			mBuffer.add((byte) 0xE0);
		}else if(buffer[0]==ShimmerObject.GET_STATUS_COMMAND) {
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x8A);
			mBuffer.add((byte) 0x71);
			mBuffer.add((byte) 0x21);
			mBuffer.add((byte) 0xF4);
		} else if(buffer[0]==ShimmerObject.INQUIRY_COMMAND) {
			inquiryResponse();
		} else if(buffer[0]==ShimmerObject.GET_BT_FW_VERSION_STR_COMMAND) {
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0xa2);
			mBuffer.add((byte) 0x35);
			byte[] bytes = UtilShimmer.hexStringToByteArray("524E343637382056312E30302E352031312F31352F32303136202863294D6963726F6368697020546563686E6F6C6F677920496E63");
			for (byte b:bytes) {
				mBuffer.add(b);
			}
			mBuffer.add((byte) 0x79);
		} else if(buffer[0]==ShimmerObject.GET_CALIB_DUMP_COMMAND) {
			if (buffer[1]==(byte)0x80 && buffer[2]==(byte)0x00 && buffer[3]==(byte)0x00) //[0x9A 0x80 0x00 0x00]
			{
				mBuffer.add((byte) 0xff);
				mBuffer.add((byte) 0x99);
				byte[] bytes = UtilShimmer.hexStringToByteArray("8000005201030002000000160702000015BE0FB95C7E330000083C081F0825005300530054019C009C0100FEFF9C1E0000150000000000000000000000000000332C332C332C009C009C000000009C1E0001150000000000000000FDC00474FFC219D31957133C009CFF9C0100FE009C1E00021500000000000000000000000000000C");
				for (byte b:bytes) {
					mBuffer.add(b);
				}
				mBuffer.add((byte) 0xB5);
			} else if (buffer[1]==(byte)0x80 && buffer[2]==(byte)0x80 && buffer[3]==(byte)0x00) //[0x9A 0x80 0x80 0x00]
			{
				mBuffer.add((byte) 0xff);
				mBuffer.add((byte) 0x99);
				byte[] bytes = UtilShimmer.hexStringToByteArray("808000D00CD00CD0009C009C000000009C1E0003150000000000000000000000000000066806680668009C009C000000009C1F00001539D4942779330000FFF5018CFD240683067F0692FF9C00640000F8FE9C1F000115000000000000000000000000000000D100D100D1009C0064000000009C1F0002150000000000000000FFEB01");
				for (byte b:bytes) {
					mBuffer.add(b);
				}
				mBuffer.add((byte) 0xFA);
			} else if (buffer[1]==(byte)0x54 && buffer[2]==(byte)0x00 && buffer[3]==(byte)0x01) //[0x9A 0x54 0x00 0x01]
			{
				mBuffer.add((byte) 0xff);
				mBuffer.add((byte) 0x99);
				byte[] bytes = UtilShimmer.hexStringToByteArray("5400015AFDF8034203410348009C0064FF00FC009C1F000315000000000000000000000000000001A201A201A2009C0064000000009C200000150000000000000000002AFBEDFC21010700E500FD009C0064000000009C");
				for (byte b:bytes) {
					mBuffer.add(b);
				}
				mBuffer.add((byte) 0x6A);
			}
		} else if(buffer[0]==ShimmerObject.SET_RWC_COMMAND) {
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0xf4);
		} 
		else if(buffer[0]==ShimmerObject.GET_RWC_COMMAND) {
			mBuffer.add((byte) 0xff);
			mBuffer.add((byte) 0x90);
			//0x00 0xEF 0xBE 0x1B 0xE7 0x09 0x3C 0x08 0x3F 0x09 0x7A 
			byte[] bytes = UtilShimmer.hexStringToByteArray("A7D7555200340000");
			for (byte b:bytes) {
				mBuffer.add(b);
			}
			mBuffer.add((byte) 0x8E);
		}else if(buffer[0]==ShimmerObject.START_STREAMING_COMMAND) {
			streaming();
		}
		else {
		
			System.out.println("Unresolved: " + UtilShimmer.bytesToHexString(buffer));
		}
		
		return true;
	}

	@Override
	public boolean setParams(int i, int j, int k, int l) throws SerialPortException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean purgePort(int i) throws SerialPortException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setByteCommunicationListener(ByteCommunicationListener byteCommListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeRadioListenerList() {
		// TODO Auto-generated method stub
		
	}

}
