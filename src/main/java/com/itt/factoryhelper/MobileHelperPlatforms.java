package com.itt.factoryhelper;

public enum MobileHelperPlatforms {

	ANDROID("Android", "android"), IOS("iOS", "ios"),;

	private String actualName;
	private String platform;

	MobileHelperPlatforms(String actualName, String platform) {
		this.actualName = actualName;
		this.platform = platform;
	}

	public String getActualName() {
		return this.actualName;
	}

	public String getPlatform() {
		return this.platform;
	}

	public static String getPlatformFor(String actualName) {
		for (MobileHelperPlatforms platforms : values()) {
			if (platforms.getActualName().equalsIgnoreCase(actualName)) {
				return platforms.getPlatform();
			}
		}
		return "";
	}

}
