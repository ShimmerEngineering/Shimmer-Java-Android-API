import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
public class SerialPortTest {
	public static final byte START_STREAMING_COMMAND          		= (byte) 0x07;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SerialPort SerialPort = new SerialPort("COM46");
		int packetSize = 23; //LN Accel + EXG TEST SIGNAL Using L&S0.11
    
        
            try {
				SerialPort.openPort();
		        SerialPort.writeByte( (byte)START_STREAMING_COMMAND );
		        SerialPort.readBytes(1, 2000);
			} catch (SerialPortException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SerialPortTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        


        for (int i = 0; i < 1000000; i++)
        {
            for (int j = 0; j < (packetSize + 1); j++) {
               
                    byte[] b;
					try {
						b = SerialPort.readBytes(1, 2000);
					    System.out.print(j + "," + b[0] + ",");
					} catch (SerialPortException e) {
						System.out.println(j + " SPEException");
						e.printStackTrace();
					} catch (SerialPortTimeoutException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println(j + " TimeoutException");
	                    break;
					}
                
                
            }
            System.out.println();
        }
		
	}

}
