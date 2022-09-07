package com.shimmerresearch.driver.shimmer2r3;

import java.nio.charset.StandardCharsets;

/**
 * @author Mark Nolan
 *
 */
public class BluetoothModuleVersionDetails {

	public enum BT_FW_VERSION {
		NOT_READ("", ""),

		RN42_VERSION_RESPONSE_V4_77("Ver 4.77 RN-42 01/05/10 \r\n(c) Roving Networks", "RN42 v4.77"),
		RN42_VERSION_RESPONSE_V6_15("Ver 6.15 04/26/2013\r\n(c) Roving Networks", "RN42 v6.15"),

		RN4678_VERSION_RESPONSE_V1_00_5("RN4678 V1.00.5 11/15/2016 (c)Microchip Technology Inc", "RN4678 v1.00.5"),
		RN4678_VERSION_RESPONSE_V1_11_0("RN4678 V1.11.00 6/1/2017 (c)Microchip Technology Inc", "RN4678 v1.00.5"),
		RN4678_VERSION_RESPONSE_V1_13_5("RN4678 V1.13.5 8/29/2018 (c)Microchip Technology Inc", "RN4678 v1.00.5"),
		RN4678_VERSION_RESPONSE_V1_22_0("RN4678 V1.22 12/08/2020 (c)Microchip Technology Inc   ", "RN4678 v1.00.5"),
		RN4678_VERSION_RESPONSE_V1_23_0("RN4678 V1.23 06/30/2021 (c)Microchip Technology Inc", "RN4678 v1.00.5"),

		UNKNOWN("Unknown", "Unknown");

		public String btFwVerStrExpected = "";
		public String btFwVerStrUserFriendly = "";

		BT_FW_VERSION(String btFwVerStrExpected, String btFwVerStrUserFriendly) {
			this.btFwVerStrExpected = btFwVerStrExpected;
			this.btFwVerStrUserFriendly = btFwVerStrUserFriendly;
		}
	}

	public BT_FW_VERSION mBtFwVersionParsed = BT_FW_VERSION.NOT_READ;
	public String mBtFwVersionReceived = mBtFwVersionParsed.btFwVerStrExpected;

	public void parseBtFwVerBytes(byte[] responseData) {
		mBtFwVersionReceived = new String(responseData, StandardCharsets.UTF_8);
		
		mBtFwVersionParsed = BT_FW_VERSION.UNKNOWN;
		for (BT_FW_VERSION btFwVersion : BT_FW_VERSION.values()) {
			if(btFwVersion.btFwVerStrExpected.equals(mBtFwVersionReceived)) {
				mBtFwVersionParsed = btFwVersion;
			}
		}
		
//		System.out.println(mBtFwVersionReceived);
//		System.out.println(mBtFwVersionParsed.btFwVerStrUserFriendly);
//		System.out.println("\n");
	}

	public String getUserFriendlyName() {
		if (mBtFwVersionParsed == BT_FW_VERSION.UNKNOWN) {
			return mBtFwVersionReceived;
		} else {
			return mBtFwVersionParsed.btFwVerStrUserFriendly;
		}
	}

}
