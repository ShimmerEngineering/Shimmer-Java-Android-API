package com.shimmerresearch.guiUtilities.configuration;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;
import com.shimmerresearch.sensors.lis2dw12.SensorLIS2DW12;
import com.shimmerresearch.sensors.lisxmdl.SensorLIS2MDL;
import com.shimmerresearch.sensors.lisxmdl.SensorLIS3MDL;
import com.shimmerresearch.sensors.lsm6dsv.SensorLSM6DSV;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;
import com.shimmerresearch.verisense.VerisenseDevice;

public class EnableLowPowerModeDialog {
	private static JDialog dialog = new JDialog();
	JPanel panel = new JPanel();
	protected ShimmerDevice shimmerDevice;
	protected ShimmerDevice clone;
	ShimmerBluetoothManager bluetoothManager;
	JCheckBox cbEnableMagLP;
	JCheckBox cbEnableGyroLP;
	JCheckBox cbEnableWRAccelLP;

	public EnableLowPowerModeDialog(ShimmerDevice shimmerPC, BasicShimmerBluetoothManagerPc btManager) {
		this.shimmerDevice = shimmerPC;
		this.bluetoothManager = btManager;
		this.clone = this.shimmerDevice.deepClone();

	}

	public static void main(String[] args) {

//		dialog.setVisible(true);

	}

	/**
	 * Call this to initialize and display the dialog.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void showDialog() {
		createFrame();
		createWriteButton();
		initialize();
		showFrame();
	}

	protected void createWriteButton() {
// TODO Auto-generated method stub
		JButton btnWriteConfig = new JButton("Save");
		btnWriteConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				boolean connected = false;
				if (shimmerDevice != null) {
					if (shimmerDevice instanceof VerisenseDevice) {
						VerisenseDevice vd = (VerisenseDevice) shimmerDevice;
						if (vd.getBluetoothRadioState().equals(BT_STATE.CONNECTED)) {
							connected = true;
						}

					} else {
						ShimmerBluetooth sb = (ShimmerBluetooth) shimmerDevice;
						if (sb.getBluetoothRadioState().equals(BT_STATE.CONNECTED)) {
							connected = true;
						}
					}
				}
				if (connected) {
					clone.setConfigValueUsingConfigLabel(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG,
							SensorLIS2MDL.GuiLabelConfig.LIS2MDL_MAG_LP, cbEnableMagLP.isSelected());
					clone.setConfigValueUsingConfigLabel(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM6DSV_GYRO,
							SensorLSM6DSV.GuiLabelConfig.LSM6DSV_GYRO_LPM, cbEnableGyroLP.isSelected());
					clone.setConfigValueUsingConfigLabel(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2DW12_ACCEL_WR,
							SensorLIS2DW12.GuiLabelConfig.LIS2DW12_ACCEL_LPM, cbEnableWRAccelLP.isSelected());

					AssembleShimmerConfig.generateSingleShimmerConfig(clone, COMMUNICATION_TYPE.BLUETOOTH);
					bluetoothManager.configureShimmer(clone);
			 		dialog.dispose();

				} else {
					JOptionPane.showMessageDialog(dialog, "Device not in a connected state!", "Info",
							JOptionPane.WARNING_MESSAGE);
				}

			}
		});
		btnWriteConfig.setToolTipText("Write the current sensors low power mode to the Shimmer device");
		dialog.getContentPane().add(btnWriteConfig, BorderLayout.SOUTH);
	}

	protected void createFrame() {
		dialog = new JDialog();
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setTitle("Low Power Mode");

		panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.Y_AXIS));

		dialog.getContentPane().add(panel, BorderLayout.CENTER);
	}

	protected void initialize() {
		createCheckBox();

		if (clone.getHardwareVersion() == HW_ID.SHIMMER_3R) {
			cbEnableGyroLP.setText("Enable LN Accel and Gyro LP Mode");
		}
		boolean isLowPowerMagEnabled = Boolean.valueOf(clone.getConfigGuiValueUsingConfigLabel(
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG, SensorLIS2MDL.GuiLabelConfig.LIS2MDL_MAG_LP));
		cbEnableMagLP.setSelected(isLowPowerMagEnabled);

		boolean isLowPowerGyroEnabled = Boolean.valueOf(clone.getConfigGuiValueUsingConfigLabel(
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM6DSV_GYRO, SensorLSM6DSV.GuiLabelConfig.LSM6DSV_GYRO_LPM));
		cbEnableGyroLP.setSelected(isLowPowerGyroEnabled);

		boolean isLowPowerWRAccelEnabled = Boolean.valueOf(
				clone.getConfigGuiValueUsingConfigLabel(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2DW12_ACCEL_WR,
						SensorLIS2DW12.GuiLabelConfig.LIS2DW12_ACCEL_LPM));
		cbEnableWRAccelLP.setSelected(isLowPowerWRAccelEnabled);

	}

	protected void createCheckBox() {
		cbEnableMagLP = new JCheckBox("Enable Mag LP Mode");
		cbEnableMagLP.setSelected(false);
		panel.add(cbEnableMagLP);

		cbEnableWRAccelLP = new JCheckBox("Enable WR Accel LP Mode", true);
		cbEnableWRAccelLP.setSelected(false);
		panel.add(cbEnableWRAccelLP);

		cbEnableGyroLP = new JCheckBox("Enable Gyro LP Mode", true);
		cbEnableGyroLP.setSelected(false);
		panel.add(cbEnableGyroLP);

	}

	protected void showFrame() {
		// TODO Auto-generated method stub
		// maybe should be in a different abstract method? lets see how android needs to
		// handles this
		dialog.setSize(300, 200);
		dialog.setVisible(true);

	}
}
