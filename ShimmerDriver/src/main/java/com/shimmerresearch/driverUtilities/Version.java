package com.shimmerresearch.driverUtilities;

public class Version {

	public int mMajor;
	public int mMinor;
	public int mInternal;
	
	/**
	 * @param version vX.X.X e.g. v2.0.0 or Y.Y.Y e.g. 1.8.0
	 */
	public Version(String version) {
		int[] parsedVersion = parseVersion(version);
        if (parsedVersion != null) {
        	mMajor = parsedVersion[0];
        	mMinor = parsedVersion[1];
        	mInternal = parsedVersion[2];
            System.out.println("Major: " + mMajor);
            System.out.println("Minor: " + mMinor);
            System.out.println("Internal: " + mInternal);
        } else {
            System.out.println("Invalid version format.");
        }
	}
	
	/**
	 * @param version vX.X.X e.g. v2.0.0 or Y.Y.Y e.g. 1.8.0
	 * @return
	 */
	public static int[] parseVersion(String version) {
        if (version.startsWith("v")) {
            version = version.substring(1); // Remove the 'v' prefix
        }
        
        String[] parts = version.split("\\.");
        if (parts.length == 3) {
            try {
                int major = Integer.parseInt(parts[0]);
                int minor = Integer.parseInt(parts[1]);
                int internal = Integer.parseInt(parts[2]);
                return new int[] { major, minor, internal };
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null; // Return null if the format is invalid
    }
	
	public String getVersion() {
		return mMajor+"."+mMinor+"."+mInternal;
	}
}
