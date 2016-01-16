package com.shimmerresearch.uartViaDock;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class ShimmerUartJssc implements ShimmerUartOsInterface {

	private final static int SHIMMER_UART_BAUD_RATE = SerialPort.BAUDRATE_115200;
	private SerialPort serialPort = null;
	public String mUniqueId = "";
	public String mComPort = "";

	public ShimmerUartJssc(String comPort, String uniqueId) {
		mUniqueId = uniqueId;
		mComPort = comPort;
	}

	/** Opens and configures the Shimmer UART COM port
	 * @throws DockException 
	 */
	@Override
	public void shimmerUartConnect() throws DockException {
        serialPort = new SerialPort(mComPort);
        try {
//        	serialPort.setRTS(true);
//        	serialPort.setDTR(false);
            serialPort.openPort();//Open serial port
            serialPort.setParams(
			            		SHIMMER_UART_BAUD_RATE, 
			            		SerialPort.DATABITS_8, 
			            		SerialPort.STOPBITS_1, 
			            		SerialPort.PARITY_NONE, 
			            		true,
			            		false);//Set params.
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        }
        catch (SerialPortException e) {
        	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
        }
        catch (Exception e) {
        	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
        }
	}
	
	
	/** Closes the Shimmer UART COM port
	 * @throws DockException 
	 */
	@Override
	public void shimmerUartDisconnect() throws DockException {
    	if(serialPort.isOpened()) {
	        try {
	        	serialPort.closePort();
			} catch (SerialPortException e) {
	        	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_CLOSING); 
	        	de.updateDockException(e.getMessage(), e.getStackTrace());
				throw(de);
			}
    	}
    }

	@Override
	public void clearSerialPortRxBuffer() throws DockException {
    	try {
			serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
		} catch (SerialPortException e) {
        	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
    }

	/** Transmits a byte array to the Shimmer UART COM port
	 * @param buf the Tx buffer array
	 * @throws DockException 
	 */
	@Override
	public void shimmerUartTxBytes(byte[] buf) throws DockException {
    	//TODO: pass rxSpeed from root method
    	int rxSpeed = 0;
    	if(rxSpeed == 0) { // normal speed
        	try {
        		for(int i = 0; i<buf.length;i++) {
        			serialPort.writeByte(buf[i]);
        			serialPort.purgePort(SerialPort.PURGE_TXCLEAR);
        		}
    		} catch (SerialPortException e) {
            	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_WRITING_DATA); 
            	de.updateDockException(e.getMessage(), e.getStackTrace());
    			throw(de);
    		}
    	}
    	else { // fast speed
        	try {
    			serialPort.writeBytes(buf);
    		} catch (SerialPortException e) {
//    			e.printStackTrace();
            	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_WRITING_DATA); 
            	de.updateDockException(e.getMessage(), e.getStackTrace());
    			throw(de);
    		}
    	}
    }    

    /** Receives a byte array from the Shimmer UART COM port
	 * @param numBytes the number of bytes to receive
	 * @return byte array of received bytes
     * @throws DockException 
	 * @see ShimmerUartRxCmdDetails
	 */
	@Override
	public byte[] shimmerUartRxBytes(int numBytes) throws DockException {
		try {
			return serialPort.readBytes(numBytes, ShimmerUart.SERIAL_PORT_TIMEOUT);
		} catch (SerialPortException e) {
        	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_READING_DATA); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
		} catch (SerialPortTimeoutException e) {
        	DockException de = new DockException(mUniqueId, mComPort,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_PORT_EXCEPTION,ErrorCodesShimmerUart.SHIMMERUART_COMM_ERR_TIMEOUT); 
        	de.updateDockException(e.getMessage(), e.getStackTrace());
			throw(de);
		}
    }

}
