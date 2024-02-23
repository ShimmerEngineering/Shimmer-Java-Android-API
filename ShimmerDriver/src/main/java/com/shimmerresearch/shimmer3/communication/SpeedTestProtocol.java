package com.shimmerresearch.shimmer3.communication;

import com.shimmerresearch.driverUtilities.ByteUtils;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.verisense.communication.AbstractByteCommunication;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.*;


public class SpeedTestProtocol {
	public interface SpeedTestResult {
		public void onNewResult(String result);
		public void onConnected();
		public void onDisconnected();
	}
	SpeedTestResult mSpeedTestListener;
	AbstractByteCommunication mByteCommunication;

    final byte[] ShimmerStopTestSignalCommand = new byte[]{ (byte)0xA4, (byte)0x00 };
    final byte[] ShimmerStartTestSignalCommand = new byte[] { (byte)0xA4, (byte)0x01 };
    static ConcurrentLinkedQueue<Byte> mQ = new ConcurrentLinkedQueue<Byte>();
    public boolean TestFirstByteReceived  = false;
    long TestSignalTotalNumberOfBytes = 0;
    long TestSignalTotalEffectiveNumberOfBytes = 0;
    long NumberofBytesDropped = 0;
    long NumberofNumbersSkipped = 0;
    public byte[] OldTestData = new byte[0];
    protected long TestSignalTSStart = 0;
    protected boolean TestSignalEnabled = false;
	/** 
	 * To initialize a connection with the Shimmer3 device
	 */
	public void connect() throws ShimmerException {
		mByteCommunication.connect();
	}

	public void setListener(SpeedTestResult listener) {
		mSpeedTestListener = listener;
    }
	
	/** 
	 * Disconnect from the Shimmer3 device
	 */
	public void disconnect() throws ShimmerException{
		mByteCommunication.disconnect();
	}
	
	public void startSpeedTest() {
		mByteCommunication.writeBytes(ShimmerStartTestSignalCommand);
	    ProcessingThread mPT = new ProcessingThread(this);
		mPT.start();
		OldTestData = new byte[0];
        TestFirstByteReceived = false;
        TestSignalTotalNumberOfBytes = 0;
        TestSignalTSStart = System.currentTimeMillis();
        TestSignalEnabled = true;
        
	}
	
	public void stopSpeedTest() {
		mByteCommunication.writeBytes(ShimmerStopTestSignalCommand);
        OldTestData = new byte[0];
        TestFirstByteReceived = false;
        TestSignalTotalNumberOfBytes = 0;
        TestSignalTotalEffectiveNumberOfBytes = 0;
        NumberofBytesDropped = 0;
        TestSignalEnabled = false;
       
	}
	
	public SpeedTestProtocol(AbstractByteCommunication byteComm) {
		mByteCommunication = byteComm;
		byteComm.setByteCommunicationListener(new ByteCommunicationListener() {

			@Override
			public void eventConnected() {
				// TODO Auto-generated method stub
				if (mSpeedTestListener!=null) {
                	mSpeedTestListener.onConnected();
                }
			}

			@Override
			public void eventDisconnected() {
				// TODO Auto-generated method stub
				if (mSpeedTestListener!=null) {
					mSpeedTestListener.onDisconnected();
                }
			}

			@Override
			public void eventNewBytesReceived(byte[] rxBytes) {
				// TODO Auto-generated method stub
				//System.out.println(rxBytes);
				for (byte b: rxBytes) {
					mQ.add(b);
				}
			}
		
		});
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public class ProcessingThread extends Thread {
		protected SpeedTestProtocol mProtocol;
		public ProcessingThread(SpeedTestProtocol protocol) {
			mProtocol = protocol;
		}
		
        public void run() {
            int lengthOfPacket = 5;
            int keepValue = 0;
            while(true) {
            	int count = mQ.size();
            	if (count>0) {
            		byte[] byteArray = new byte[count];
            		for (int i=0;i<count;i++) {
            			byteArray[i]= mQ.poll();
            		}
            		System.out.println(UtilShimmer.bytesToHexString(byteArray));
            		
            		if (!mProtocol.TestFirstByteReceived)
                    {
                        TestFirstByteReceived = true;
                        //ProgrammerUtilities.CopyAndRemoveBytes(ref buffer, 1);
                        byteArray = ByteUtils.removeFirstByte(byteArray);
                        System.out.println(UtilShimmer.bytesToHexString(byteArray));
                    }
            		TestSignalTotalNumberOfBytes += byteArray.length;
            		
            		byte[] data = ByteUtils.joinArrays(OldTestData, byteArray);
            		while(data.length >= lengthOfPacket+1)
                    {
            			
            			 if (data[0] == (byte)0XA5 && data[5] == (byte)0XA5)
                         {
            				 byte[] bytesFullPacket = new byte[lengthOfPacket];	
             				 System.arraycopy(data, 0, bytesFullPacket, 0, lengthOfPacket);
            				 TestSignalTotalEffectiveNumberOfBytes += 5;
            				 byte[] bytes = new byte[lengthOfPacket - 1];
            				 System.arraycopy(bytesFullPacket, 1, bytes, 0, bytes.length);
            				 //System.arraycopy(bytesFullPacket, 1, bytes, 0, bytes.length);
            				 ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN);
            				 int intValue = buffer.getInt();
            				 data = ByteUtils.removeFirstBytes(data, lengthOfPacket);
            				 //System.out.println("COUNT : " + intValue );
            				 
            				 if (keepValue != 0)
                             {
                                 int difference = intValue - keepValue;
                                 if ((difference) != 1)
                                 {
                                     NumberofNumbersSkipped += difference;
                                 }
                             }
            				 
            				 keepValue = intValue;
            				 
                         } else
                         {
                             data = ByteUtils.removeFirstByte(data);
                             NumberofBytesDropped++;
                         }
                    }
            		long testSignalCurrentTime = System.currentTimeMillis();
                    long duration = (long) ((testSignalCurrentTime - TestSignalTSStart) / 1000.0); //make it seconds
                    if (duration!=0) {
	                    String result = "Effective Throughput (bytes per second): " + (TestSignalTotalEffectiveNumberOfBytes / duration) + ", Number of Bytes Dropped: " + NumberofBytesDropped + ", Numbers Skipped: " + NumberofNumbersSkipped + ", (Duration S): " + duration + "";
	                    System.out.println(result);
	                    if (mSpeedTestListener!=null) {
	                    	mSpeedTestListener.onNewResult(result);
	                    }
                    }
            		OldTestData = data;
            	}
            }
        }
    }

}
