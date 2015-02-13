THINGS TO DO
- updates log fragment and hear rate fragment, moving core functionality , log and ppg-hr algorithm to the service so screen orientation changes wont cause them to restart 
- When Using Shimmer 3, ensure your gyroscope has been calibrated for the default range of 500dps using the SHimmer 9DoF Calibration Application

Changes since Java/Android ID 2.3 release
- updated service method, cleaner and easier to understand, especially when passing the service to the fragment
- Added and updated PPG to HR algorithm

Changes Since Shimmer.java Rev 1.3
- fixed a bug with the method used to count the number of signals for the plot fragment
- added notification , and using startforeground service now
- added logging fragment
 