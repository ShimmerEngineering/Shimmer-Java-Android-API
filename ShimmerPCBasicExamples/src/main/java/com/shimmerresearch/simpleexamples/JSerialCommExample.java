package com.shimmerresearch.simpleexamples;

import com.fazecast.jSerialComm.SerialPort;
import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.pcSerialPort.SerialPortCommJSerialComm;

/**
 * Example demonstrating how to list available serial ports and create
 * a jSerialComm-based connection for better macOS Bluetooth support.
 * 
 * This example shows:
 * 1. How to list all available COM ports
 * 2. How to create a jSerialComm connection directly
 * 3. Basic connection setup
 * 
 * Note: For most use cases, you should use ShimmerPC which automatically
 * uses jSerialComm for better reliability.
 * 
 * @author Shimmer Research
 */
public class JSerialCommExample {

    public static void main(String[] args) {
        // Example 1: List all available serial ports
        listAvailablePorts();
        
        // Example 2: Create a jSerialComm connection (not connected yet)
        // Replace "COM12" with your actual port name
        String comPort = "COM12";
        demonstrateJSerialCommSetup(comPort);
    }
    
    /**
     * Lists all available serial ports on the system
     */
    public static void listAvailablePorts() {
        System.out.println("=== Available Serial Ports ===");
        SerialPort[] ports = SerialPort.getCommPorts();
        
        if (ports.length == 0) {
            System.out.println("No serial ports found.");
            return;
        }
        
        for (SerialPort port : ports) {
            System.out.println("Port: " + port.getSystemPortName());
            System.out.println("  Description: " + port.getDescriptivePortName());
            System.out.println("  Location: " + port.getPortLocation());
            System.out.println();
        }
    }
    
    /**
     * Demonstrates how to set up a jSerialComm connection
     * @param comPort The COM port to connect to (e.g., "COM12" on Windows, "/dev/tty.Shimmer3-xxxx" on macOS)
     */
    public static void demonstrateJSerialCommSetup(String comPort) {
        System.out.println("=== jSerialComm Setup Example ===");
        System.out.println("Setting up connection to: " + comPort);
        
        try {
            // Create a jSerialComm connection
            SerialPortCommJSerialComm serialPort = new SerialPortCommJSerialComm(
                comPort,          // COM port name
                "shimmer-demo",   // Unique identifier
                115200            // Baud rate (standard for Shimmer devices)
            );
            
            // Configure timeout (optional)
            serialPort.setTimeout(AbstractSerialPortHal.SERIAL_PORT_TIMEOUT_500);
            
            System.out.println("✓ Serial port connection object created successfully");
            System.out.println("  Port: " + comPort);
            System.out.println("  Baud Rate: 115200");
            System.out.println("  Timeout: 500ms");
            System.out.println();
            System.out.println("Note: This example only creates the connection object.");
            System.out.println("To actually connect, call: serialPort.connect()");
            System.out.println();
            System.out.println("For a complete working example, see ShimmerPCExample.java");
            
        } catch (Exception e) {
            System.err.println("✗ Failed to create serial port connection");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Additional notes for macOS users
     */
    public static void printMacOSNotes() {
        System.out.println("=== Notes for macOS Users ===");
        System.out.println();
        System.out.println("1. Bluetooth Port Names:");
        System.out.println("   - Classic Bluetooth ports typically appear as:");
        System.out.println("     /dev/tty.Shimmer3-xxxx or /dev/cu.Shimmer3-xxxx");
        System.out.println();
        System.out.println("2. Permissions:");
        System.out.println("   - You may need to grant Bluetooth permissions in:");
        System.out.println("     System Preferences → Security & Privacy → Privacy");
        System.out.println("   - Add your application to 'Bluetooth' and 'Input Monitoring'");
        System.out.println();
        System.out.println("3. Port Discovery:");
        System.out.println("   - Use the listAvailablePorts() method to find your device");
        System.out.println("   - Make sure the Shimmer device is paired in System Preferences");
        System.out.println();
        System.out.println("4. Known Issues Resolved:");
        System.out.println("   - Connection timeout issues → Fixed with jSerialComm");
        System.out.println("   - Port not responding → Improved with native macOS support");
        System.out.println("   - Data loss during transmission → Better buffer management");
        System.out.println();
    }
}
