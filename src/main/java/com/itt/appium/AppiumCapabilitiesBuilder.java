package com.itt.appium;


import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.collect.ImmutableMap;
import com.itt.android.adb.AdbFactory;
import com.itt.common.DeviceInfo;


public class AppiumCapabilitiesBuilder {

	private static final String USER_DIR = System.getProperty("user.dir");
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	private static DeviceInfo deviceInfoObj;
	private static DesiredCapabilities capabilities;

	public static DesiredCapabilities buildBasicAndroidTestCapabilities(DeviceInfo deviceInfo) {
		capabilities = new DesiredCapabilities();
		deviceInfoObj = deviceInfo;
		capabilities.setCapability("platformName", "Android");
		capabilities.setCapability("deviceName", deviceInfoObj.getModelName());
		capabilities.setCapability("session-override", "true");
		capabilities.setCapability("debug-log-spacing", "true");
		capabilities.setCapability("noSign", "true");
		capabilities.setCapability("newCommandTimeout", "2000000");// 33.33 mins
		capabilities.setCapability("deviceReadyTimeout", "120000");
		capabilities.setCapability("platformVersion", deviceInfoObj.getOsVersion());
		capabilities.setCapability("udid", deviceInfo.getDeviceID());
		capabilities.setCapability("browserName", "Chrome");
		capabilities.setCapability("appium:chromeOptions", ImmutableMap.of("w3c", false));
		String pinStoredInDevice = AdbFactory.getAdbController()
				.executeAdbCommand(" shell cat " + "/sdcard/" + deviceInfo.getDeviceID() + ".txt");
		if (pinStoredInDevice != null && pinStoredInDevice.length() != 0) {
			pinStoredInDevice = pinStoredInDevice.trim();
			capabilities.setCapability("unlockType", "password");
			capabilities.setCapability("unlockKey", pinStoredInDevice);
		}

		return capabilities;
	}

	public static DesiredCapabilities buildBasicAndroidTestCapabilities(String deviceUdid, String osVersion,
			String modelName) {
		capabilities = new DesiredCapabilities();
		capabilities.setCapability("platformName", "Android");
		/* capabilities.setCapability(CapabilityType.BROWSER_NAME, "Android"); */
		capabilities.setCapability("deviceName", modelName);
		capabilities.setCapability("session-override", "true");
		capabilities.setCapability("debug-log-spacing", "true");
		capabilities.setCapability("noSign", "true");
		capabilities.setCapability("newCommandTimeout", "2000000");// 33.33 mins
		capabilities.setCapability("deviceReadyTimeout", "120000");
		capabilities.setCapability("platformVersion", osVersion);
		capabilities.setCapability("udid", deviceUdid);
		capabilities.setCapability("browserName", "Chrome");
		capabilities.setCapability("appium:chromeOptions", ImmutableMap.of("w3c", false));
		String pinStoredInDevice = AdbFactory.getAdbController()
				.executeAdbCommand(" shell cat " + "/sdcard/" + deviceUdid + ".txt");
		if (pinStoredInDevice != null && pinStoredInDevice.length() != 0) {
			pinStoredInDevice = pinStoredInDevice.trim();
			capabilities.setCapability("unlockType", "password");
			capabilities.setCapability("unlockKey", pinStoredInDevice);
		}

		return capabilities;
	}

	public static DesiredCapabilities buildUIAutomator2AndroidCapabilities(String deviceUdid, String osVersion,
			String modelName) {
		DesiredCapabilities capabilities = buildBasicAndroidTestCapabilities(deviceUdid, osVersion, modelName);
		capabilities.setCapability("automationName", "UiAutomator2");
		return capabilities;
	}

	public static DesiredCapabilities buildUIAutomator2AndroidCapabilities(DeviceInfo deviceInfo) {
		DesiredCapabilities capabilities = buildBasicAndroidTestCapabilities(deviceInfo);
		capabilities.setCapability("automationName", "UiAutomator2");
		return capabilities;
	}
}
