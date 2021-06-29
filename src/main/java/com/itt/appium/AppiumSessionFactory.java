package com.itt.appium;

import java.net.URL;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.ttl.TransmittableThreadLocal;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class AppiumSessionFactory {

	private static final Logger LOG = LoggerFactory.getLogger(AppiumSessionFactory.class);

	private static ThreadLocal<AndroidDriver<WebElement>> androidDriverSessions = new TransmittableThreadLocal<AndroidDriver<WebElement>>();

	public static AndroidDriver<WebElement> getAndroidDriverSession(URL url, DesiredCapabilities capabilities)
			throws Exception {
		setNewAndroidSession(url, capabilities);
		return androidDriverSessions.get();
	}

	public static void quitAndroidDriverSession() {
		try {
			androidDriverSessions.get().quit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			androidDriverSessions.remove();
		}

	}

	private static void setNewAndroidSession(URL url, DesiredCapabilities capabilities) {
		if (androidDriverSessions.get() == null) {
			AndroidDriver<WebElement> androidDriver = new AndroidDriver<WebElement>(url, capabilities);
			androidDriverSessions.set(androidDriver);
		}
	}

}
