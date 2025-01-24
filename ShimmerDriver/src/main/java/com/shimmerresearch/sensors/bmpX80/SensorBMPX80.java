package com.shimmerresearch.sensors.bmpX80;

import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.bmpX80.SensorBMP180.DatabaseConfigHandle;

public abstract class SensorBMPX80 extends AbstractSensor {

	private static final long serialVersionUID = 1772340408277892416L;

	//--------- Sensor specific variables start --------------

	protected int mPressureResolution = 0;

	public CalibDetailsBmpX80 mCalibDetailsBmpX80 = new CalibDetailsBmp180(); // = new CalibDetailsBmp280();

	public class GuiLabelConfig{
		public static final String PRESSURE_RESOLUTION = "Pressure Resolution";
		public static final String PRESSURE_RATE = "Pressure Rate";
	}

	public class GuiLabelSensors{
		public static final String PRESS_TEMP_BMPX80 = "Pressure & Temperature";
	}
	
	// GUI Sensor Tiles
	public class LABEL_SENSOR_TILE{
		public static final String PRESSURE_TEMPERATURE = GuiLabelSensors.PRESS_TEMP_BMPX80;
	}
	
	public abstract void setPressureResolution(int i);
	public abstract List<Double> getPressTempConfigValuesLegacy();

	//--------- Sensor specific variables end --------------

	
	public SensorBMPX80(SENSORS sensorType, ShimmerVerObject svo) {
		super(sensorType, svo);
		// TODO Auto-generated constructor stub
	}
	
	public SensorBMPX80(SENSORS sensorType, ShimmerDevice shimmerDevice) {
		super(sensorType, shimmerDevice);
		// TODO Auto-generated constructor stub
	}


	public byte[] getRawCalibrationParameters(ShimmerVerObject svo){        
		byte[] rawcal=new byte[1];
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen3R() || mShimmerVerObject.isShimmerGen4()){
			// Mag + Pressure
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
			try {
				outputStream.write(5); // write the number of different calibration parameters

				outputStream.write( mCalibDetailsBmpX80.getPressureRawCoefficients().length);
				outputStream.write( mCalibDetailsBmpX80.getPressureRawCoefficients() );
				rawcal = outputStream.toByteArray( );
			} catch (IOException e) {
				e.printStackTrace();
			}			

		} 
		else {
			rawcal[0]=0;
		}
		return rawcal;
	}

	public void parseCalParamByteArray(byte[] pressureResoRes, CALIB_READ_SOURCE calibReadSource) {
		mCalibDetailsBmpX80.parseCalParamByteArray(pressureResoRes, calibReadSource);
	}

	public double[] calibratePressureSensorData(double uP, double uT) {
		return mCalibDetailsBmpX80.calibratePressureSensorData(uP, uT);
	}
	
	public void retrievePressureCalibrationParametersFromPacket(byte[] pressureResoRes, CALIB_READ_SOURCE calibReadSource) {
		mCalibDetailsBmpX80.parseCalParamByteArray(pressureResoRes, calibReadSource);
		mCalibDetailsBmpX80.mRangeValue = getPressureResolution();
	}
	
	public int getPressureResolution(){
		return mPressureResolution;
	}
	
	public void updateCurrentPressureCalibInUse(){
		mCalibDetailsBmpX80.mRangeValue = getPressureResolution();
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;

			configBytes[configByteLayoutCast.idxConfigSetupByte3] |= (byte) ((getPressureResolution() & configByteLayoutCast.maskBMPX80PressureResolution) << configByteLayoutCast.bitShiftBMPX80PressureResolution);
		}

//		System.out.println("Info Mem Pressure resolution:\t" + mPressureResolution_BMP280);
//		System.out.println("Check");
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
			setPressureResolution((configBytes[configByteLayoutCast.idxConfigSetupByte3] >> configByteLayoutCast.bitShiftBMPX80PressureResolution) & configByteLayoutCast.maskBMPX80PressureResolution);
		}
	}
	
}
