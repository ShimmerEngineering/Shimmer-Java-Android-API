package com.shimmerresearch.driver.ble;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamShimmer {

	InputStream IS;

	public InputStreamShimmer(InputStream is) {
		IS = is;
	}

	public byte[] readBytes(int length) {
		byte[] readBytes = new byte[length];
		try {
			IS.read(readBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return readBytes;
	}
}
