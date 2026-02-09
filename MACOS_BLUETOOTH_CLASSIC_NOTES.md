# MacOS Bluetooth Classic Serial Port Communication - Known Issues and Workarounds

## Overview

This document describes the comprehensive investigation into MacOS Bluetooth Classic serial port communication issues with Shimmer devices and the fixes that have been implemented.

## Implemented Fixes

The following fixes have been implemented to address MacOS-specific serial port communication issues:

### 1. Serial Port Detection
- **Issue**: MacOS serial ports were being routed to BLE handler
- **Fix**: Changed `COMPORT_PREFIX_MAC` from `"Shimmer"` to `"/dev/"`
- **Location**: `ShimmerBluetoothManager.java`

### 2. Port Type Selection
- **Issue**: `/dev/tty.*` ports block on carrier detect
- **Fix**: Auto-convert to `/dev/cu.*` (call-out, non-blocking variant)
- **Location**: `SerialPortCommJssc.java`

### 3. Connection Race Condition
- **Issue**: Callbacks fired before serial port reader initialization
- **Fix**: Move `eventDeviceConnected()` after `startSerialPortReader()`
- **Location**: `SerialPortCommJssc.java`

### 4. Extended Timing Delays
- **Issue**: Device needs time to stabilize and respond
- **Fix**: 
  - Port initialization: 500ms (increased from 100ms)
  - Command-response delay: 500ms (increased from 200ms)
  - Signal stabilization: 50ms (new)
- **Location**: `SerialPortCommJssc.java`, `ShimmerRadioInitializer.java`

### 5. Instance-based Delay Control
- **Issue**: Static variable caused shared state between connections
- **Fix**: Changed `mUseLegacyDelayToDelayForResponse` to instance variable
- **Location**: `ShimmerRadioInitializer.java`

### 6. Extended Read Timeout
- **Issue**: 500ms timeout too short for MacOS delays
- **Fix**: Extended to 2000ms on MacOS
- **Location**: `SerialPortCommJssc.java`

### 7. Hardware Handshaking
- **Issue**: DTR signal not activated when set as parameter
- **Fix**: 
  - Enable DTR in constructor
  - Explicitly call `setRTS(true)` and `setDTR(true)` after configuration
- **Location**: `SerialPortCommJssc.java`

### 8. Buffer Management
- **Issue**: Stale data from previous connections
- **Fix**: Purge RX/TX buffers on connect
- **Location**: `SerialPortCommJssc.java`

## Debug Output

When connecting on MacOS, you should see:
```
ShimmerRadioInitializer: Constructor called with delay flag = true
MacOS detected: Using cu port instead of tty: /dev/cu.Shimmer3-XXXX
MacOS: Using extended serial port timeout (2000ms) and DTR enabled
Connecting to COM port:/dev/cu.Shimmer3-XXXX
Port open: true
Params set: true
Port Status : true
MacOS: Purged serial port buffers
MacOS: Explicitly set RTS=true and DTR=true
MacOS: Port initialization delay completed (500ms)
ShimmerRadioInitializer: Applying command-response delay (500ms)
```

## Known Limitations

Despite all implemented fixes, Bluetooth Classic serial port communication on MacOS may still experience issues due to:

### 1. JSSC Library Limitations
The Java Simple Serial Connector (JSSC) library may have fundamental compatibility issues with MacOS Bluetooth serial ports that cannot be resolved through configuration alone.

### 2. MacOS Bluetooth Stack
MacOS Bluetooth implementation, particularly in newer versions, may not fully support Bluetooth Classic SPP (Serial Port Profile) in the same way as Windows/Linux.

### 3. Device Compatibility
Some Shimmer device firmware versions may not properly implement Bluetooth Classic in a MacOS-compatible way.

## Alternative Solutions

If Bluetooth Classic continues to fail on MacOS, consider these alternatives:

### Option 1: Use Bluetooth Low Energy (BLE)
If your Shimmer device supports BLE, this protocol generally has better MacOS support:
- More modern protocol
- Better MacOS integration
- Lower power consumption

### Option 2: USB Connection
If the device supports USB connectivity:
- Bypass Bluetooth entirely
- More reliable connection
- Faster data transfer

### Option 3: Virtual Machine
Run Windows or Linux in a virtual machine:
- VirtualBox or Parallels
- Pass through Bluetooth adapter
- Use Windows/Linux driver stack

### Option 4: Different Serial Library
Consider alternative Java serial libraries:
- **jSerialComm**: More actively maintained, better cross-platform support
- **PureJavaComm**: Pure Java implementation
- **RxTx**: Classic library with good MacOS support

## Troubleshooting Steps

### 1. Verify Device Pairing
```bash
# List paired Bluetooth devices
system_profiler SPBluetoothDataType

# Check if serial ports are created
ls -la /dev/cu.* | grep Shimmer
ls -la /dev/tty.* | grep Shimmer
```

### 2. Test with screen command
```bash
# Try connecting directly (replace XXXX with your device ID)
screen /dev/cu.Shimmer3-XXXX 115200
```

### 3. Check JSSC Version
Verify you're using a recent JSSC version that has MacOS fixes.

### 4. Monitor Serial Port
```bash
# Monitor what's being sent/received
cat /dev/cu.Shimmer3-XXXX
```

## Configuration Summary

For reference, the complete MacOS configuration is:

| Parameter | Value | Purpose |
|-----------|-------|---------|
| Port Type | `/dev/cu.*` | Non-blocking call-out device |
| Baud Rate | 115200 | Standard rate |
| Data Bits | 8 | Standard |
| Stop Bits | 1 | Standard |
| Parity | None | Standard |
| Flow Control | None | No hardware flow control |
| RTS | true | Request To Send |
| DTR | true | Data Terminal Ready |
| Buffer Purge | Both RX/TX | Clear stale data |
| Signal Stabilization | 50ms | After setting RTS/DTR |
| Port Initialization | 500ms | Port hardware settling |
| Command-Response Delay | 500ms | Device processing time |
| Read Timeout | 2000ms | Response wait window |

## Technical Details

### Port Types on MacOS
- `/dev/tty.Device` - Terminal device (blocks on carrier detect)
- `/dev/cu.Device` - Call-out device (non-blocking, preferred)

Both ports connect to the same device, but `cu` is recommended for outgoing connections.

### Timing Requirements
Total time from port open to first read attempt:
- Port configuration: <10ms
- Buffer purge: <10ms
- Signal setting: <10ms
- Signal stabilization: 50ms
- Port initialization: 500ms
- Command transmission: <10ms
- Command-response delay: 500ms
- **Total: ~1070ms**

This is significantly longer than Windows/Linux (~300ms) due to MacOS Bluetooth stack requirements.

## Support

If you continue to experience issues:

1. **Check device works on other platforms** - Verify the issue is MacOS-specific
2. **Try different MacOS version** - Some versions have better Bluetooth support
3. **Contact Shimmer support** - They may have MacOS-specific guidance
4. **Consider filing JSSC issue** - If confirmed as library bug
5. **Use alternative connection method** - BLE or USB if available

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-02-09 | 1.0 | Initial comprehensive MacOS fix implementation |

## References

- [JSSC GitHub Repository](https://github.com/java-native/jssc)
- [MacOS Serial Port Programming Guide](https://developer.apple.com/library/archive/documentation/DeviceDrivers/Conceptual/IOKitFundamentals/SerialDriver/SerialDriver.html)
- [Shimmer Documentation](https://www.shimmersensing.com/)
