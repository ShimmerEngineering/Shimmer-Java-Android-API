import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test to verify that jSerialComm classes are available at runtime
 * This addresses the NoClassDefFoundError issue reported by users
 */
public class JSerialCommAvailabilityTest {

    @Test
    public void testJSerialCommClassesAvailable() {
        try {
            // Test that we can load the SerialPort class
            Class<?> serialPortClass = Class.forName("com.fazecast.jSerialComm.SerialPort");
            assertNotNull("SerialPort class should be loadable", serialPortClass);
            
            // Test that we can load the SerialPortDataListener class
            Class<?> listenerClass = Class.forName("com.fazecast.jSerialComm.SerialPortDataListener");
            assertNotNull("SerialPortDataListener class should be loadable", listenerClass);
            
            System.out.println("✓ jSerialComm classes are available at runtime");
        } catch (ClassNotFoundException e) {
            fail("jSerialComm classes should be available on classpath: " + e.getMessage());
        }
    }
    
    @Test
    public void testSerialPortCommJSerialCommAvailable() {
        try {
            // Test that our jSerialComm implementation classes are available
            Class<?> serialPortCommClass = Class.forName("com.shimmerresearch.pcSerialPort.SerialPortCommJSerialComm");
            assertNotNull("SerialPortCommJSerialComm class should be loadable", serialPortCommClass);
            
            Class<?> byteCommClass = Class.forName("com.shimmerresearch.shimmer3.communication.ByteCommunicationJSerialComm");
            assertNotNull("ByteCommunicationJSerialComm class should be loadable", byteCommClass);
            
            System.out.println("✓ jSerialComm implementation classes are available");
        } catch (ClassNotFoundException e) {
            fail("jSerialComm implementation classes should be available: " + e.getMessage());
        }
    }
}
