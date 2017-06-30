package com.shimmerresearch.tools.bluetooth;

import javax.swing.JFrame;

public class BluetootManagerTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BasicShimmerBluetoothManagerPc manager = new BasicShimmerBluetoothManagerPc();
		manager.connectShimmerThroughCommPort("COM35");
		JFrame n = new JFrame();
				n.setVisible(true);
		n.setSize(200, 200);
		n.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
