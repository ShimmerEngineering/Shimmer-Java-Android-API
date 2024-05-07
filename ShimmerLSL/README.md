# ShimmerLSL: Streaming Shimmer Data with LabRecorder
This guide outlines how to stream data from your Shimmer device using LSL (Lab Streaming Layer) and record it with LabRecorder.

Update btComports:

- Open the SendData.java file.

- Replace "Com5" and "Com7" with the actual COM ports where your Shimmer devices are connected. Ensure each COM port matches a unique device.

Download LabRecorder:

- Visit the LabRecorder GitHub repository: https://github.com/labstreaminglayer/App-LabRecorder/releases
- Download the latest release compatible with your operating system (Win_amd64 for Windows 64-bit, Win_i386 for Windows 32-bit).
- Extract the LabRecorder file to your selected destination.

Run SendData.java:

- Compile and run the program SendData.java

Start LabRecorder:

- Open the downloaded LabRecorder application from the extracted path. 

Update Stream List:

- If no streams appear in the LabRecorder list, click the "Update" button.

Select Stream:

- Locate the stream corresponding to the COM port you used in btComports. It should be named something like "SendData_DeviceX_ComX_SensorName" (where X represents the device number and COM port, SensorName represents the sensor being streamed).
- Click the checkbox next to the desired stream to select it.

Set Modality:

- In the bottom-right corner of LabRecorder, find the "Modality (%m)" update to "Accel" or any sensor that being recorded.

Start Recording:

- Click the "Start" button in LabRecorder to begin recording the streaming data.

Stop Recording:

- Once you're finished recording, click the "Stop" button.

Recorded Data Location:

- By default, LabRecorder saves recorded data files in XDF format. The location depends on your operating system:
Windows: The file will be saved in "C:\Users\X\Documents\CurrentStudy\sub-P001\ses-S001\"Modality (%m)"" (replace "X" with your username).

Download SigViewer:

- Visit the SigViewer GitHub repository: https://github.com/cbrnr/sigviewer?tab=readme-ov-file
- Download the latest release compatible with your operating system (Windows, macOS, Linux).
- Install the SigViewer to your computer.

Visualize Streamed Data:

- In SigViewer, go to File > Open and navigate to the Recorded Data Location.
- The recorded data stream will be visualized on the SigViewer.
