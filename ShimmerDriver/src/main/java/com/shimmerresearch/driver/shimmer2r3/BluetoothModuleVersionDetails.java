package com.shimmerresearch.driver.shimmer2r3;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * @author Mark Nolan
 *
 */
public class BluetoothModuleVersionDetails implements Serializable {

	private static final long serialVersionUID = -4602970064289358794L;

	public static enum BT_MODULE_VERSION {
		NOT_READ("", "", ""),

		RN42_VERSION_RESPONSE_V4_77("Ver 4.77 RN-42 01/05/10 \r\n(c) Roving Networks", "Ver 4.77 RN-42", "RN42 v4.77"),
		RN42_VERSION_RESPONSE_V6_15("Ver 6.15 04/26/2013\r\n(c) Roving Networks", "Ver 6.15 04", "RN42 v6.15"),

		RN4678_VERSION_RESPONSE_V1_00_5("RN4678 V1.00.5 11/15/2016 (c)Microchip Technology Inc", "RN4678 V1.00.5 ", "RN4678 v1.00.5"),
		RN4678_VERSION_RESPONSE_V1_11_0("RN4678 V1.11.00 6/1/2017 (c)Microchip Technology Inc", "RN4678 V1.11.00 ", "RN4678 v1.11.0"),
		RN4678_VERSION_RESPONSE_V1_13_5("RN4678 V1.13.5 8/29/2018 (c)Microchip Technology Inc", "RN4678 V1.13.5 ", "RN4678 v1.15.5"),
		RN4678_VERSION_RESPONSE_V1_22_0("RN4678 V1.22 12/08/2020 (c)Microchip Technology Inc   ", "RN4678 V1.22 ", "RN4678 v1.23"),
		RN4678_VERSION_RESPONSE_V1_23_0("RN4678 V1.23 06/30/2021 (c)Microchip Technology Inc", "RN4678 V1.23 ", "RN4678 v1.23"),

		UNKNOWN("Unknown", "Unknown", "Unknown");

		public String btModuleVerStrFull = "";
		public String btModuleVerStrComparison = "";
		public String btModuleVerStrUserFriendly = "";

		BT_MODULE_VERSION(String btModuleVerStrExpected, String btModuleVerStrComparison, String btModuleVerStrUserFriendly) {
			this.btModuleVerStrFull = btModuleVerStrExpected;
			this.btModuleVerStrComparison = btModuleVerStrComparison;
			this.btModuleVerStrUserFriendly = btModuleVerStrUserFriendly;
		}
	}

	public BT_MODULE_VERSION mBtModelVersionParsed = BT_MODULE_VERSION.NOT_READ;
	public String mBtModelVersionReceived = mBtModelVersionParsed.btModuleVerStrFull;

	public void parseBtModuleVerBytes(byte[] responseData) {
		mBtModelVersionReceived = new String(responseData, StandardCharsets.UTF_8);
		
		mBtModelVersionParsed = BT_MODULE_VERSION.UNKNOWN;
		for (BT_MODULE_VERSION btModuleVersion : BT_MODULE_VERSION.values()) {
			if(mBtModelVersionReceived.contains(btModuleVersion.btModuleVerStrComparison)) {
				mBtModelVersionParsed = btModuleVersion;
			}
		}
	}

	public String getUserFriendlyName() {
		if (mBtModelVersionParsed == BT_MODULE_VERSION.UNKNOWN) {
			return mBtModelVersionReceived;
		} else {
			return mBtModelVersionParsed.btModuleVerStrUserFriendly;
		}
	}

}
