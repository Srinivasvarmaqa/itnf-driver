package com.itt.factoryhelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.itt.common.BrowserInfo;
import com.itt.common.Timeout;
import com.itt.context.ITTDriverContext;

public class BrowserHelperFactory implements BrowserHelperFactoryI {

	private static final Logger LOG = LoggerFactory.getLogger(BrowserHelperFactory.class);
	private static ThreadLocal<BrowserHelperFactoryI> browserSupport = new TransmittableThreadLocal<BrowserHelperFactoryI>();
	private WebDriverWait wait;
	private static final String NAME = "name";
	private static final String ID = "id";
	private static final String CSSSELECTOR = "cssSelector";
	private static final String XPATH = "xpath";
	private static final String CLASS = "class";
	private static final String TEXT = "text";
	private static final String PARTIALTEXT = "partialtext";
	private static final String CONTENTDESC = "content-desc";
	private static final String PASSWORD = "password";
	private static final String TYPE = "type";
	private static final String PARTIALID = "partialId";
	private static final String INDEX = "index";
	private static final String VALUE = "value";
	private static final String VISIBLETEXT = "visibleText";
	private static final String CHROME = "chrome";
	private static final String FIREFOX = "firefox";
	private static final String EDGE = "edge";
	private static final String ALERT_ACCEPT = "accept";
	private static final String ALERT_DISMISS = "dismiss";
	private static final String ALERT_TEXT = "alertText";
	private static final String ALERT_INPUT = "alertInput";


	public static BrowserHelperFactoryI getBrowserDriver() {
		return browserSupport.get();
	}

	public enum DROPDOWN {
		INDEX("index"), VALUE("value"), VISIBLETEXT("visibleText");
		private String dropdownMethodName;

		private DROPDOWN(String dropdownMethodName) {
			this.dropdownMethodName = dropdownMethodName;
		}

		public String toString() {
			return this.dropdownMethodName;
		}
	}

	public enum ALERT {
		ACCEPT("accept"), DISMISS("dismiss"), ALERT_TEXT("alertText"), ALERT_INPUT("alertInput");
		private String alertAction;

		private ALERT(String alertAction) {
			this.alertAction = alertAction;
		}

		public String toString() {
			return this.alertAction;
		}
	}

	public synchronized static void initBrowserDriver(BrowserInfo browserInfo) throws Exception {

		if (browserInfo == null) {
			LOG.error("Browser Info object is null");
		}
		String browser = browserInfo.getBrowser().toLowerCase();
		switch (browser) {
			case CHROME :
				browserSupport.set(new ChromeBrowserDriver(browserInfo));
				break;
			case FIREFOX :
				browserSupport.set(new FirefoxBrowserDriver(browserInfo));
				break;
			case EDGE :
				browserSupport.set(new EdgeBrowserDriver(browserInfo));
				break;
			default :
				LOG.error("Error! unknown browser " + browser);
				throw new Exception("Incorrect Browser Name");
		}
	}

	public static void setDriver(BrowserHelperFactoryI driver) throws Exception {
		browserSupport.set(driver);
	}

	@Override
	public void invokeDriver() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public ITTDriverContext getDriverContext() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void openUrl(String url) throws Exception {
		this.getWebDriver().get(url);
	}
	
	@Override
	public boolean isElementPresent(HashMap<String, String> params) throws Exception {
		boolean isElementPresent = false;
		WebElement element;
		By by = getByFromParams(params);
		try {
			if (params.containsKey("visibility")) {
				wait = this.getWebDriverWait(Timeout.TEN_SECONDS_TIMEOUT);
				element = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			} else {
				element = this.getWebDriver().findElement(by);
			}
			isElementPresent = element.isDisplayed();
		} catch (Exception e) {
			LOG.debug("Exception during isElement Present Check - " + by.toString());
		}
		return isElementPresent;
	}

	private By getByFromParams(HashMap<String, String> params) throws Exception {
		By by = null;
		if (null == params.get("searchBy")) {
			return null;
		}
		switch (params.get("searchBy")) {
			case ID :
				by = By.id(params.get("searchValue"));
				break;
			case NAME :
				by = By.name(params.get("searchValue"));
				break;
			case CSSSELECTOR :
				by = By.cssSelector(params.get("searchValue"));
				break;
			case XPATH :
				by = By.xpath(params.get("searchValue"));
				break;
			case CLASS :
				by = By.className(params.get("searchValue"));
				break;
			case TEXT :
				by = By.xpath("//*[@text='" + params.get("searchValue") + "']");
				break;
			case PARTIALTEXT :
				by = By.xpath("//*[contains(@text,'" + params.get("searchValue")
						+ "')]");
				break;
			case CONTENTDESC :
				by = By.xpath("//*[@content-desc='" + params.get("searchValue")
						+ "']");
				break;
			case PASSWORD :
				by = By.xpath(
						"//*[@password='" + params.get("searchValue") + "']");
				break;
			case TYPE :
				by = By.xpath("//*[@type='" + params.get("searchValue") + "']");
				break;
			case PARTIALID :
				by = By.xpath("//*[contains(@id,'" + params.get("searchValue")
						+ "')]");
				break;
			default :
				throw new Exception("Incorrect identifier!");
		}
		return by;

	}

	@Override
	public void closeBrowser() throws Exception {
		this.getWebDriver().close();
	}

	@Override
	public void click(HashMap<String, String> params) throws Exception {
		By by = getByFromParams(params);
		if (params.containsKey("scrollTo")) {
			this.scrollTo(params);
		}
		this.waitForElement(params);
		WebElement element = this.getWebDriver().findElement(by);
		LOG.debug("Wait for element to be clickable");
		wait = this.getWebDriverWait(Timeout.TWENTY_SECONDS_TIMEOUT);
		element = wait.until(ExpectedConditions.elementToBeClickable(element));
		element.click();
	}

	@Override
	public boolean waitForElement(HashMap<String, String> params) {
		try {
			final By by = getByFromParams(params);
			int waitTime = Integer.parseInt(params.get("timeout"));
			if (waitTime != 0) {
				wait = this.getWebDriverWait(waitTime);
			} else {
				wait = this.getWebDriverWait(Timeout.TEN_SECONDS_TIMEOUT);
			}
			LOG.debug("Wait for element to be visible for max time:" + waitTime);
			String visibility = "true";
			if (params.containsKey("visibility")) {
				visibility = params.get("visibility");
			}
			WebElement element = waitForVisibleElement(by, waitTime, visibility);
			if (visibility.equals("true")) {
				if (null != element) {
					return true;
				} else {
					return false;
				}

			} else {
				if (null == element) {
					return true;
				} else {
					return false;
				}
			}

		} catch (Exception ex) {
			return false;
		}
	}

	private WebElement waitForVisibleElement(final By by, int waitTime, String visibility) throws Exception {
		WebElement element = null;
		LOG.debug("By= " + by.toString());
		wait = this.getWebDriverWait(waitTime);
		for (int attempt = 0; attempt < waitTime; attempt++) {
			try {
				element = this.getWebDriver().findElement(by);
				if (visibility.equals("true")) {
					break;
				} else if (visibility.equals("false")) {
					LOG.debug("Started waiting for element to disappear...");
					long startTime = System.currentTimeMillis();
					wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
					long endTime = System.currentTimeMillis();
					element = null;
					LOG.debug("Element disappeared after {} seconds.", (endTime - startTime) / 1000);
					break;
				}
			} catch (Exception exc) {
				if (visibility.equals("false")) {
					break;
				}
			}
		}
        Wait<WebDriver> fluentWait = new FluentWait<WebDriver>(this.getWebDriver())
                .withTimeout(waitTime, TimeUnit.SECONDS)
                .pollingEvery(500, TimeUnit.MILLISECONDS)
                .ignoring(NoSuchElementException.class);
        long startTime = System.currentTimeMillis();
		if (visibility.equals("true")) {
			startTime = System.currentTimeMillis();
			element = fluentWait.until(ExpectedConditions.presenceOfElementLocated(by));
			if (null != element && element.isDisplayed()) {
				return element;
			} else if (null != element) {
				fluentWait.until(ExpectedConditions.visibilityOf(element));
			}
		} else {
			if (element != null) {
				wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
				element = null;
			}
		}
		long endTime = System.currentTimeMillis();
		LOG.debug("**** Element displayed after {} seconds.", (endTime - startTime) / 1000 + " ****");
		return element;
	}

	@Override
	public WebElement findElement(HashMap<String, String> params) throws Exception {
		By by = getByFromParams(params);
		WebElement element = this.getWebDriver().findElement(by);
		return element;
	}

	@Override
	public List<WebElement> findElements(HashMap<String, String> params) throws Exception {
		By by = getByFromParams(params);
		List<WebElement> elements = this.getWebDriver().findElements(by);
		return elements;
	}

	@Override
	public WebDriver getWebDriver() throws Exception {
		return this.getWebDriver();
	}

	@Override
	public WebDriverWait getWebDriverWait(int timeout) throws Exception {
		return new WebDriverWait(this.getWebDriver(), timeout);
	}

	@Override
	public void waitForPageLoad() throws Exception {
		JavascriptExecutor js = (JavascriptExecutor) this.getWebDriver();
		int i = 0;
		try {
			for (i = 0; i < 5; i++) {
				if ((Boolean) js.executeScript(
						"return (window.self.name == '') && (document.readyState == 'complete');")) {
					return;
				} else if ((Boolean) js.executeScript(
						"return (window.self.name != '') && (jQuery.active <= 1) && (document.readyState == 'complete');")) {
					return;
				} else {
					Thread.sleep(1000);
				}
			}
		} catch (Exception e) {
			LOG.debug("wait for page load check failed" + e.getMessage());
		}
		 LOG.debug("PAGE IS NOT LOADED SUCCESSFULLY EVEN AFTER WAITING FOR SECONDS:" + i);
	}

	@Override
	public boolean isAlertPresent() throws Exception {
		try {
			this.getWebDriver().switchTo().alert();
			return true;
		} catch (NoAlertPresentException Ex) {
			return false;
		}
	}

	@Override
	public void refreshPage() throws Exception {
		this.getWebDriver().navigate().refresh();
		this.waitForPageLoad();
	}

	@Override
	public String getCurrentUrl() throws Exception {
		return this.getWebDriver().getCurrentUrl();
	}

	@Override
	public String getPageTitle() throws Exception {
		return this.getWebDriver().getTitle();
	}

	@Override
	public void switchToMainWindow() throws Exception {
		this.getWebDriver().switchTo().defaultContent();
	}

	@Override
	public void switchToWindow(HashMap<String, String> params) throws Exception {
		this.getWebDriver().switchTo().window(params.get("nameOrHandle"));
	}

	@Override
	public void switchToFrame(HashMap<String, String> params) throws Exception {
		String nameOrHandle = params.get("nameOrHandle");
		boolean switchToWindow = params.get("switchToMainWindow") == "true";

		if (switchToWindow || params.get("switchToMainWindow") == null) {
			this.switchToMainWindow();
		}

		wait = this.getWebDriverWait(Timeout.TEN_SECONDS_TIMEOUT);
		if (!StringUtils.isEmpty(params.get("searchBy"))) {
			By by = getByFromParams(params);
			WebElement element = null;
			try {
				element = this.getWebDriver().findElement(by);
				wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			} catch (Exception e) {
				LOG.error("Element cannot be located by {}", by.toString());
			}
			if (null != element) {
				this.getWebDriver().switchTo().frame(element);
			}
		} else if (params.containsKey("index")) {
			String index = params.get("index");
			Integer indexInt = Integer.parseInt(index);
			this.getWebDriver().switchTo().frame(indexInt);
		} else {
			this.getWebDriver().switchTo().frame(nameOrHandle);
		}
		waitForPageLoad();
	}

	@Override
	public WebElement getCurrentFrame() throws Exception {
		try {
			WebElement frame = (WebElement) ((JavascriptExecutor) this.getWebDriver()).executeScript("return window.frameElement");
			return frame;
		} catch (Exception e) {
			LOG.debug("Couldn't get the current frame {}", e.getMessage());
			return null;
		}
	}

	@Override
	public String getAttributeValue(HashMap<String, String> params) throws Exception {
		By by = getByFromParams(params);
		return this.getWebDriver().findElement(by).getAttribute(params.get("attribute"));
	}

	@Override
	public String getAttributeValue(WebElement element, String attribute) throws Exception {
		return element.getAttribute("attribute");
	}

	@Override
	public void clearTextField(WebElement element) {
		element.clear();
	}

	@Override
	public WebElement findElementWithTimeout(By by, int time) throws Exception {
		WebElement element = null;
		for (int i = 0; i < time; i++) {
			try {
				element = this.getWebDriver().findElement(by);
				if (null != element ) {
					LOG.debug("Found the element by {}, Returning the element", by.toString());
				}
				break;
			} catch (NoSuchElementException nsee) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					LOG.debug("Element not found by {} till time", i);
				}
			}
		}
		return element;
	}

	@Override
	public void sendValue(HashMap<String, String> params) throws Exception {
		this.waitForElement(params);
		WebElement element = findElement(params);
		boolean clearField = params.get("clear") != null;
		String value = params.get("value"); // in this case the value will be text

		if (clearField) {
			element.clear();
		} else {
			element.click();
		}
		element.sendKeys(value);
	}

	@Override
	public String getText(HashMap<String, String> params) throws Exception {
		this.waitForElement(params);
		try {
			if (!isElementPresent(params))
				return null;
			return findElement(params).getText();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void selectDropDown(HashMap<String, String> params) throws Exception {
		By by = getByFromParams(params);
		if (params.containsKey("value") && params.get("value") == null ) {
			LOG.debug("Value is null");
		} else {
			this.waitForElement(params);
			Select dropdown = new Select(this.getWebDriver().findElement(by));
			String dropdownMethodType = params.get("type");
			switch (dropdownMethodType) {
				case INDEX :
					String index = params.get("value");
					Integer indexInt = Integer.parseInt(index);
					dropdown.selectByIndex(indexInt);
					break;
				case VALUE :
					dropdown.selectByValue(params.get("value"));
					break;
				case VISIBLETEXT :
					dropdown.selectByVisibleText(params.get("value"));
					break;
				default :
					LOG.error("Error! unknown dropdown option: " + dropdownMethodType);
					throw new Exception("Incorrect dropdown selection");
			}
		}
	}

	@Override
	public boolean waitForAlert() throws Exception {
		try {
			wait = this.getWebDriverWait(Timeout.TEN_SECONDS_TIMEOUT);
	        wait.until(ExpectedConditions.alertIsPresent());
	        return true;
		} catch (Exception e) {
			LOG.debug("Alert is NOT Displayed");
			return false;
		}
	}

	@Override
	public HashMap<String, String> handleAlerts(HashMap<String, String> params) throws Exception {
		String alertAction = params.get("alertAction");
		this.waitForAlert();
		switch (alertAction) {
			case ALERT_ACCEPT :
				getWebDriver().switchTo().alert().accept();
				break;
			case ALERT_DISMISS :
				getWebDriver().switchTo().alert().dismiss();
				break;
			case ALERT_TEXT :
				String alertText = getWebDriver().switchTo().alert().getText();
				params.put("alertText", alertText);
				break;
			case ALERT_INPUT:
				String alertInput = params.get("alertInput");
				getWebDriver().switchTo().alert().sendKeys(alertInput);
				break;
			default :
				LOG.error("Error! unknown alert action: " + alertAction);
				throw new Exception("Incorrect Alert Action");
		}
		return params;
	}

	@Override
	public void closeAllChildWindowPopups() throws Exception {
	    String parentWindowHandle= (String)this.getDriverContext().getAttribute("PARENT_WINDOW_HANDLE_ID");

		try {
			for (String winHandle : this.getWebDriver().getWindowHandles()) {
				if (!parentWindowHandle.equals(winHandle)) {
					WebDriver windowPopup = this.getWebDriver().switchTo().window(winHandle);
					LOG.debug("Window Title is:" + windowPopup.getTitle());
					windowPopup.close();
				}
			}
		} catch (Exception e) {
			LOG.debug("Failed to close the child window");
		} finally {
			this.getWebDriver().switchTo().window(parentWindowHandle);
		}
	}

	@Override
	public String executeJavaScript(String cmd) throws Exception {
		LOG.debug("Executing javascript with command:" + cmd);
		JavascriptExecutor js = (JavascriptExecutor) this.getWebDriver();
		return (String)js.executeScript(cmd);
	}

	@Override
	public void takeScreenShot(HashMap<String, String> params) throws Exception {
		try {
			Path srcFile = ((TakesScreenshot) this.getWebDriver()).getScreenshotAs(OutputType.FILE).toPath();
			Path target = new File(params.get("FilePath") + ".png").toPath();
			Files.copy(srcFile, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOG.debug("Exception while taking screenshot! "  + e.getMessage());
		}
	}

	@Override
	public String getPageSource() throws Exception {
		return getWebDriver().getPageSource();
	}

	@Override
	public void quit() throws Exception {
		this.getWebDriver().quit();
	}

	@Override
	public void close() throws Exception {
		this.getWebDriver().close();
	}

	@Override
	public void scrollPageDown() throws Exception {
		LOG.debug("Scroll page Down");
		JavascriptExecutor js = (JavascriptExecutor) this.getWebDriver();
		js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}

	@Override
	public void scrollPageUp() throws Exception {
		LOG.debug("Scroll page Up");
		JavascriptExecutor js = (JavascriptExecutor) this.getWebDriver();
		js.executeScript("window.scrollTo(0, -document.body.scrollHeight)");
	}

	@Override
	public void scrollTo(HashMap<String, String> params) throws Exception {
		LOG.debug("Scroll till the element is found");
		final By by = getByFromParams(params);
		JavascriptExecutor js = (JavascriptExecutor) this.getWebDriver();
		js.executeScript("arguments[0].scrollIntoView(true);", this.getWebDriver().findElement(by));
	}

	@Override
	public void moveToElement(HashMap<String, String> params) throws Exception {
		LOG.debug("Move to known element");
		final By by = getByFromParams(params);
		Actions actions = new Actions(this.getWebDriver());
		actions.moveToElement(this.getWebDriver().findElement(by));
		actions.release().perform();
	}

    @Override
    public void sendSpecialKeys(Keys key) {
	try {
		this.getWebDriver().switchTo().activeElement().sendKeys(key);
	} catch (Exception e) {
		LOG.info("UNABLE TO SEND KEYS=======");
		e.printStackTrace();
	}
}
}
