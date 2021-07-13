package com.itt.factoryhelper;

import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.itt.context.ITTDriverContext;

public interface BrowserHelperFactoryI {

	public void invokeDriver() throws Exception;

	public void openUrl(String url) throws Exception;

	public void closeBrowser() throws Exception;

	public void click(HashMap<String, String> params) throws Exception;

	public boolean isElementPresent(HashMap<String, String> params) throws Exception;

	public boolean waitForElement(HashMap<String, String> params) throws Exception;

	public WebElement findElementWithTimeout(By by, int timeout) throws Exception;

	public WebElement findElement(HashMap<String, String> params) throws Exception;

	public WebDriver getWebDriver() throws Exception;

	public WebDriverWait getWebDriverWait(int timeout) throws Exception;

	public void waitForPageLoad() throws Exception;

	public boolean isAlertPresent() throws Exception;

	public void refreshPage() throws Exception;

	public String getCurrentUrl() throws Exception;

	public String getPageTitle() throws Exception;

	public void switchToFrame(HashMap<String, String> params) throws Exception;

	public void switchToWindow(HashMap<String, String> params) throws Exception;

	public void switchToMainWindow() throws Exception;

	public WebElement getCurrentFrame() throws Exception;

	public String getAttributeValue(HashMap<String, String> params) throws Exception;

	public String getAttributeValue(WebElement element, String attribute) throws Exception;

	public void sendValue(HashMap<String, String> params) throws Exception;

	public String getText(HashMap<String, String> params) throws Exception;

	public List<WebElement> findElements(HashMap<String, String> params) throws Exception;

	public void clearTextField(WebElement element);

	public void selectDropDown(HashMap<String, String> params) throws Exception;

	public HashMap<String, String> handleAlerts(HashMap<String, String> params) throws Exception;

	public boolean waitForAlert() throws Exception;

	public String executeJavaScript(String cmd) throws Exception;

	public void closeAllChildWindowPopups() throws Exception;

	public void takeScreenShot(HashMap<String, String> params) throws Exception;

	public String getPageSource() throws Exception;

	public ITTDriverContext getDriverContext() throws Exception;

	public void quit() throws Exception;

	public void close() throws Exception;

	public void scrollPageDown() throws Exception;

	public void scrollPageUp() throws Exception;

	public void scrollTo(HashMap<String, String> params) throws Exception;

}
