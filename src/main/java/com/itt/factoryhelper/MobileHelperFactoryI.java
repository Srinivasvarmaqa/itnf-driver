package com.itt.factoryhelper;

import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.itt.context.ITTDriverContext;

public interface MobileHelperFactoryI {

	public void launchApp(HashMap<String, String> params) throws Exception;

	public void click(HashMap<String, String> params) throws Exception;

	public void sendValue(HashMap<String, String> params) throws Exception;

	public void longPress(HashMap<String, String> params) throws Exception;

	public Boolean isElementPresent(HashMap<String, String> params) throws Exception;

	public void hideKeyBoard();

	public Boolean isKeyboardShown();

	public void clearApplicationData(HashMap<String, String> params) throws Exception;

	public Boolean installApplication(HashMap<String, String> params);

	public void uninstallApplication(HashMap<String, String> params);

	public void sendSpecialKeys(String key);

	public void changeDriverType(String mode);

	public void wakeUpDevice() throws Exception;

	public void unlockDevice(HashMap<String, String> params) throws Exception;

	public void closeApplication(HashMap<String, String> params) throws Exception;

	public String getText(HashMap<String, String> params);

	public void scrollUp(HashMap<String, String> params) throws Exception;

	public void scrollDown(HashMap<String, String> params) throws Exception;

	public void scrollTo(HashMap<String, String> params);

	public void pressHome() throws Exception;

	public void pressBackButton();

	public void swipeLeft(HashMap<String, String> params) throws Exception;

	public void swipeRight(HashMap<String, String> params) throws Exception;

	public void longswipeRight(HashMap<String, String> params) throws Exception;

	public Boolean waitForElement(HashMap<String, String> params);

	public String getElementProperty(HashMap<String, String> params) throws Exception;

	public Boolean verifyElementProperty(HashMap<String, String> params) throws Exception;

	@SuppressWarnings("rawtypes")
	public List findElements(HashMap<String, String> params) throws Exception;

	public WebElement findElement(By by);

	public Boolean isApplicationInstalled(HashMap<String, String> params);

	public void takeDeviceScreenShot(HashMap<String, String> params) throws Exception;

	public void openNotificationTray() throws Exception;

	public void startLogging(String filePath) throws Exception;

	public void stopLogging() throws Exception;

	public void changeDeviceOrientation(HashMap<String, String> params) throws Exception;

	public void rebootDevice();

	public void lockDevice();

	public void dragDown(HashMap<String, String> params) throws Exception;

	public void setPickerValues(HashMap<String, String> params) throws Exception;

	public void setLocation(HashMap<String, String> params) throws Exception;

	public void clearLocation();

	public String getCurrentApplication();

	public void setProperty(HashMap<String, String> params) throws Exception;

	public void switchTo(HashMap<String, String> params) throws Exception;

	public void tap(HashMap<String, String> params) throws Exception;

	public String getAttributeValue(HashMap<String, String> params) throws Exception;

	public void swipeOrScrollByCoordinates(HashMap<String, String> params) throws Exception;

	boolean isAppinForeGround(HashMap<String, String> params) throws Exception;

	boolean isAppKilled(HashMap<String, String> params) throws Exception;

	boolean isAppinBackGround(HashMap<String, String> params) throws Exception;

	public void executeNativeCommand(String string, HashMap<String, Object> params) throws Exception;

	public String getPageSource() throws Exception;

	public <T> T getDeviceController() throws Exception;

	public void startRecordingScreen(int timeOutinSeconds) throws Exception;

	public void stopRecordingScreen(String filePath) throws Exception;

	public ITTDriverContext getDriverContext() throws Exception;
	
	public void navigateToUrl(String url) throws Exception;
	
	public void refreshPage() throws Exception;

}
