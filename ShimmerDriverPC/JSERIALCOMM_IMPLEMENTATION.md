# jSerialComm Implementation for Better macOS Support

## Overview

This implementation adds support for jSerialComm library as an alternative to JSSC (Java Simple Serial Connector) for better macOS Bluetooth Classic communications. The jSerialComm library provides more reliable port connection and response handling on macOS.

## What Changed

### New Classes Added

1. **SerialPortCommJSerialComm** - Extends `AbstractSerialPortHal`
   - Main serial port handler using jSerialComm
   - Located in: `com.shimmerresearch.pcSerialPort.SerialPortCommJSerialComm`

2. **JSerialCommByteWriter** - Implements `ByteWriter`
   - Handles byte writing with timeout support
   - Located in: `com.shimmerresearch.pcSerialPort.JSerialCommByteWriter`

3. **ByteCommunicationJSerialComm** - Implements `ByteCommunication`
   - Byte-level communication wrapper for jSerialComm
   - Located in: `com.shimmerresearch.shimmer3.communication.ByteCommunicationJSerialComm`

### Updated Classes

1. **BasicShimmerBluetoothManagerPc**
   - `createNewSerialPortComm()` now creates `SerialPortCommJSerialComm` instances
   - Backward compatible with existing JSSC code

2. **ShimmerPC**
   - Added overloaded `setSerialPort(com.fazecast.jSerialComm.SerialPort)` method
   - Supports both JSSC and jSerialComm serial ports

3. **build.gradle**
   - Added jSerialComm dependency: `com.fazecast:jSerialComm:2.11.0`

## Usage

### Default Behavior (No Code Changes Required)

The new implementation is now the default for new connections. Existing code using `ShimmerPC` and `BasicShimmerBluetoothManagerPc` will automatically use jSerialComm:

```java
// This now uses jSerialComm by default
ShimmerPC shimmer = new ShimmerPC("COM12");
shimmer.connect("COM12", "");
```

### Direct Usage

If you need to create serial port communications directly:

```java
// Using jSerialComm
SerialPortCommJSerialComm serialPort = new SerialPortCommJSerialComm(
    "COM12",          // COM port
    "uniqueId",       // Unique identifier
    115200            // Baud rate
);

serialPort.setTimeout(AbstractSerialPortHal.SERIAL_PORT_TIMEOUT_500);
serialPort.connect();
```

### For Legacy JSSC Code

The original JSSC implementation is still available and can be used:

```java
// Using JSSC (legacy)
SerialPortCommJssc serialPort = new SerialPortCommJssc(
    "COM12", 
    "uniqueId", 
    SerialPort.BAUDRATE_115200
);
```

## Benefits of jSerialComm

1. **Better macOS Support**: Native macOS Bluetooth support resolves connection and response issues
2. **Active Development**: jSerialComm is actively maintained with regular updates
3. **Pure Java**: No native library dependencies reduce deployment complexity
4. **Cross-Platform**: Works consistently across Windows, macOS, and Linux
5. **Thread-Safe**: Better handling of concurrent port access

## Compatibility

- ✅ Backward compatible with existing JSSC code
- ✅ All existing tests pass
- ✅ No API changes for end users
- ✅ Both implementations can coexist during transition

## Troubleshooting

### Port Not Found

If you're having trouble finding ports on macOS, try:
```java
SerialPort[] ports = SerialPort.getCommPorts();
for (SerialPort port : ports) {
    System.out.println(port.getSystemPortName());
}
```

### Permission Issues on macOS

On macOS, you may need to grant permissions to access Bluetooth devices:
1. System Preferences → Security & Privacy → Privacy
2. Add your application to "Bluetooth" and "Input Monitoring"

### Connection Timeout

If connections are timing out, increase the timeout:
```java
serialPort.setTimeout(AbstractSerialPortHal.SERIAL_PORT_TIMEOUT_2000);
```

## Migration Guide

For developers who want to explicitly use jSerialComm in custom code:

### Before (JSSC)
```java
import jssc.SerialPort;
import com.shimmerresearch.pcSerialPort.SerialPortCommJssc;

SerialPortCommJssc comm = new SerialPortCommJssc(
    comPort, 
    uniqueId, 
    SerialPort.BAUDRATE_115200
);
```

### After (jSerialComm)
```java
import com.fazecast.jSerialComm.SerialPort;
import com.shimmerresearch.pcSerialPort.SerialPortCommJSerialComm;

SerialPortCommJSerialComm comm = new SerialPortCommJSerialComm(
    comPort, 
    uniqueId, 
    115200
);
```

## Technical Details

### Architecture

The implementation follows the same abstraction pattern as JSSC:

```
AbstractSerialPortHal (interface/abstract)
    ├── SerialPortCommJssc (JSSC implementation)
    └── SerialPortCommJSerialComm (jSerialComm implementation)

ByteCommunication (interface)
    ├── ByteCommunicationJSSC (JSSC implementation)
    └── ByteCommunicationJSerialComm (jSerialComm implementation)
```

### Exception Handling

jSerialComm exceptions are mapped to ShimmerException for consistency:
- Connection failures → `ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_PORT_EXCEPTON_OPENING`
- Read/Write failures → `ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_READING_DATA`
- Timeout failures → `ErrorCodesSerialPort.SHIMMERUART_COMM_ERR_TIMEOUT`

## Testing

All existing tests pass with the new implementation. The test suite includes:
- Serial port connection tests
- Data transmission tests
- Timeout handling tests
- Event listener tests

Run tests with:
```bash
./gradlew test
```

## Support

For issues or questions:
1. Check the [GitHub Issues](https://github.com/ShimmerEngineering/Shimmer-Java-Android-API/issues)
2. Review the jSerialComm documentation: https://github.com/Fazecast/jSerialComm
3. Contact Shimmer Research support

## References

- jSerialComm GitHub: https://github.com/Fazecast/jSerialComm
- jSerialComm Wiki: https://github.com/Fazecast/jSerialComm/wiki
- Original issue: macOS Bluetooth Classic communication problems with JSSC
