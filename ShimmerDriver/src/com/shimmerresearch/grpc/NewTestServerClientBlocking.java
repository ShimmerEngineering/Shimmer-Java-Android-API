package com.shimmerresearch.grpc;

import javax.swing.JFrame;

public class NewTestServerClientBlocking {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ShimmerClientGrpc hwc = new ShimmerClientGrpc("Localhost",50051);
		JFrame frame = new JFrame();
		frame.setSize(100,100);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
	}

}
