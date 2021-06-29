package com.itt.factoryhelper;

import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.itt.android.drivers.AppiumAndroidDriver;
import com.itt.common.DeviceInfo;

public abstract class MobileHelperFactory implements MobileHelperFactoryI {

	public synchronized static void ITTInitDriver(final DeviceInfo deviceInfo, final String executionRunId)
			throws Exception {

		LOG.info("Device platform name : " + deviceInfo.getPlatformName());
		switch (MobileHelperPlatforms.valueOf(deviceInfo.getPlatformName().toUpperCase())) {

		case IOS:
			LOG.info("Initializing ITT Driver for iOS");
			break;
		case ANDROID:
			LOG.info("Initializing ITT Driver for Android");
			MobileHelperFactory.setDriver(new AppiumAndroidDriver(deviceInfo, executionRunId));
			break;

		default:
			throw new Exception("Incorrect Platform name");

		}

	}

	private static final Logger LOG = LoggerFactory.getLogger(MobileHelperFactory.class);
	private static ThreadLocal<MobileHelperFactoryI> mobilesHelperFactory = new TransmittableThreadLocal<MobileHelperFactoryI>();

	public static MobileHelperFactoryI getMobileDriver() {
		return mobilesHelperFactory.get();
	}

	protected abstract void quit(HashMap<String, String> params);

	public enum DriverType {
		NATIVE(0), WEB(1);

		private final int value;

		private DriverType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public static void setDriver(MobileHelperFactoryI driver) throws Exception {
		mobilesHelperFactory.set(driver);
	}

	// General
	public abstract void launchApp(HashMap<String, String> params) throws Exception;

	public abstract void click(HashMap<String, String> params) throws Exception;

	public abstract void sendValue(HashMap<String, String> params) throws Exception;

	public abstract void longPress(HashMap<String, String> params) throws Exception;

	public abstract Boolean isElementPresent(HashMap<String, String> params) throws Exception;

	public abstract void hideKeyBoard();

	public abstract void clearApplicationData(HashMap<String, String> params) throws Exception;

	public abstract Boolean installApplication(HashMap<String, String> params);

	public abstract void uninstallApplication(HashMap<String, String> params);

	public abstract void sendSpecialKeys(String key);

	public abstract void changeDriverType(String mode);

	public abstract void wakeUpDevice() throws Exception;

	public abstract void unlockDevice(HashMap<String, String> params) throws Exception;

	public abstract void closeApplication(HashMap<String, String> params) throws Exception;

	public abstract String getText(HashMap<String, String> params);

	public abstract void scrollUp(HashMap<String, String> params) throws Exception;

	public abstract void scrollDown(HashMap<String, String> params) throws Exception;

	public abstract void scrollTo(HashMap<String, String> params);

	public abstract void pressHome() throws Exception;

	public abstract void pressBackButton();

	public abstract void swipeLeft(HashMap<String, String> params) throws Exception;

	public abstract void swipeRight(HashMap<String, String> params) throws Exception;

	public abstract Boolean waitForElement(HashMap<String, String> params);

	public abstract String getElementProperty(HashMap<String, String> params) throws Exception;

	public abstract Boolean verifyElementProperty(HashMap<String, String> params) throws Exception;

	@SuppressWarnings("rawtypes")
	public abstract List findElements(HashMap<String, String> params) throws Exception;

	public abstract WebElement findElement(By by);

	public abstract Boolean isApplicationInstalled(HashMap<String, String> params);

	public abstract void takeDeviceScreenShot(HashMap<String, String> params) throws Exception;

	public abstract void openNotificationTray() throws Exception;

	public abstract void startLogging(String filePath) throws Exception;

	public abstract void stopLogging() throws Exception;

	public abstract void changeDeviceOrientation(HashMap<String, String> params) throws Exception;

	public abstract void rebootDevice();

	public abstract void lockDevice();

	public abstract void dragDown(HashMap<String, String> params) throws Exception;

	public abstract void setPickerValues(HashMap<String, String> params) throws Exception;

	public abstract void setLocation(HashMap<String, String> params) throws Exception;

	public abstract void clearLocation();

	public abstract String getCurrentApplication();

	public abstract void setProperty(HashMap<String, String> params) throws Exception;

	public abstract String getAttributeValue(HashMap<String, String> params) throws Exception;

	public abstract void executeNativeCommand(String command, HashMap<String, Object> params) throws Exception;

}
