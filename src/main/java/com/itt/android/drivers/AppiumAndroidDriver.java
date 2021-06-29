package com.itt.android.drivers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.imageio.spi.IIORegistry;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itt.android.adb.AdbController;
import com.itt.android.adb.AdbFactory;
import com.itt.appium.AppiumCapabilitiesBuilder;
import com.itt.appium.AppiumServerFactory;
import com.itt.appium.AppiumSessionFactory;
import com.itt.appium.InvokeAppiumServer;
import com.itt.common.*;
import com.itt.common.DeviceInfo;
import com.itt.context.ITTDriverConstants;
import com.itt.context.ITTDriverContext;
import com.itt.factoryhelper.MobileHelperFactory;
import com.itt.ssh.SshClient;
import com.itt.ssh.SshClientFactory;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import io.appium.java_client.screenrecording.CanRecordScreen;
import io.appium.java_client.touch.LongPressOptions;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.ElementOption;
import io.appium.java_client.touch.offset.PointOption;
import org.sikuli.script.Region;

public class AppiumAndroidDriver extends MobileHelperFactory {

	private final Logger LOG = LoggerFactory.getLogger(AppiumAndroidDriver.class);
	private AndroidDriver<WebElement> androidDriver;
	private WebDriverWait wait;
	private DeviceInfo deviceInfoObj;
	private AdbController adbController;
	private final String APPIUM_SETTINGS_PACKAGE = "io.appium.settings";
	private final String APPIUM_IO_UNOCK_PACKAGE = "io.appium.unlock";
	private final String VYSOR_APP_PACKAGE = "com.koushikdutta.vysor";
	private final String APPIUM_UIAUTOMATOR2_PACKAGE = "io.appium.uiautomator2";
	private final String APPIUM_UIAUTOMATOR2_SERVER_PACKAGE = "io.appium.uiautomator2.server";
	private final String APPIUM_UIAUTOMATOR2_TEST_PACKAGE = "io.appium.uiautomator2.server.test";
	private final boolean IS_SCROLL_ABSOLUTE = true;
	public String deviceUDID;
	private ITTDriverContext ittDriverContext;
	private SshClient sshClientObj;
	private URL url;

	
	public AppiumAndroidDriver(final DeviceInfo deviceInfo, String executionRunId) throws Exception {

		this.deviceInfoObj = deviceInfo;
		this.ittDriverContext = new ITTDriverContext();
		DesiredCapabilities capabilities;
		// To Support Local Execution, Need to Create Local UDID in case if it is not
		// passed from caller method
		if (executionRunId == null || executionRunId.length() <= 0) {
			executionRunId = UUID.randomUUID().toString();
			this.ittDriverContext.setAttribute(ITTDriverConstants.IS_COPYING_ARTIFACTS_FROM_DEVICEHUB_REQUIRED_KEY,
					true);

		}
		this.deviceInfoObj.setExecutionRunId(executionRunId);
		if (this.deviceInfoObj.isRemoteDevice()) {
			
			this.sshClientObj = new SshClient(this.deviceInfoObj.getiPAddress(), this.deviceInfoObj.getLoginPassword(),
					this.deviceInfoObj.getLoginUserName(), true);
			SshClientFactory.setSshClient(sshClientObj);
			
			String tempRemoteDeviceExecutionFolderPath = String.format(ITTDriverConstants.DEVICE_TEMP_FILE_DIR_PATH,
					executionRunId, this.deviceInfoObj.getDeviceID());
			String tempRemoteDeviceTestResourceFolderPath = tempRemoteDeviceExecutionFolderPath
					+ ITTDriverConstants.FILE_SEPARATOR + ITTDriverConstants.TEST_RESOUCES_FOLDER_NAME;
			String tempRemoteDeviceLogsFolderPath = tempRemoteDeviceExecutionFolderPath
					+ ITTDriverConstants.FILE_SEPARATOR + ITTDriverConstants.DEVICE_LOGS_FOLDER_NAME;
			this.ittDriverContext.setAttribute(ITTDriverConstants.TEST_EXECUTION_ID_KEY, executionRunId);
			this.ittDriverContext.setAttribute(ITTDriverConstants.DEVICE_TEMP_FILE_DIR_KEY,
					tempRemoteDeviceExecutionFolderPath);
			this.ittDriverContext.setAttribute(ITTDriverConstants.TEST_RESOURCES_TEMP_FILE_DIR_KEY,
					tempRemoteDeviceTestResourceFolderPath);
			this.ittDriverContext.setAttribute(ITTDriverConstants.TEST_DEVICELOGS_TEMPFOLDER_KEY,
					tempRemoteDeviceLogsFolderPath);
			this.sshClientObj.createFolderPath(
					this.ittDriverContext.getAttribute(ITTDriverConstants.TEST_RESOURCES_TEMP_FILE_DIR_KEY).toString());
			this.sshClientObj.createFolderPath(
					this.ittDriverContext.getAttribute(ITTDriverConstants.TEST_DEVICELOGS_TEMPFOLDER_KEY).toString());
		}

		if (null != deviceInfoObj.getDeviceName() && deviceInfoObj.getDeviceName().contains("UI2")) {
			LOG.info("CREATE ADB CONTROLLER OBJECT AND PUSH TO ADBFACTORY");
			adbController = new AdbController(deviceInfoObj, ittDriverContext);
			AdbFactory.setAdbControllerObj(adbController);
			capabilities = AppiumCapabilitiesBuilder.buildUIAutomator2AndroidCapabilities(deviceInfoObj);
		} else {
			LOG.info("CREATE ADB CONTROLLER OBJECT AND PUSH TO ADBFACTORY");
			adbController = new AdbController(deviceInfoObj, ittDriverContext);
			AdbFactory.setAdbControllerObj(adbController);
			capabilities = AppiumCapabilitiesBuilder.buildBasicAndroidTestCapabilities(deviceInfoObj);
		}

		try {
			InvokeAppiumServer invokeAppiumServer = new InvokeAppiumServer(deviceInfoObj);
			if (this.deviceInfoObj.isRemoteDevice()) {
				this.ittDriverContext.setAttribute(ITTDriverConstants.DEVICE_HUB_APPIUM_LOG_PATH_KEY,
						invokeAppiumServer.getAPPIUM_LOG_FILE().getParent());
			}
			LOG.info("CREATE ADB CONTROLLER OBJECT");
			LOG.info("UNINSTALL EXISTING APPIUM APPS FROM DEVICE BEFOER STARTING APPIUM SESSION");
			adbController.uninstallPackage(APPIUM_IO_UNOCK_PACKAGE);
			adbController.uninstallPackage(APPIUM_SETTINGS_PACKAGE);
			adbController.uninstallPackage(VYSOR_APP_PACKAGE);

			LOG.info("STOPPING THE UIAUTOMATOR2 SERVICE BEFORE CREATING THE APPIUM SESSION");
			LOG.info("STOPPING THE UIAUTOMATOR2 SERVICE BEFORE CREATING THE APPIUM SESSION");
			adbController.uninstallPackage(APPIUM_UIAUTOMATOR2_PACKAGE);
			adbController.uninstallPackage(APPIUM_UIAUTOMATOR2_SERVER_PACKAGE);
			adbController.uninstallPackage(APPIUM_UIAUTOMATOR2_TEST_PACKAGE);

			LOG.info("WAKING UP THE DEVICE BEFORE CREATING SESSION");
			adbController.executeAdbCommand(" shell input keyevent 224");

			LOG.info("START APPIUM SESSION");
			url = new URL(invokeAppiumServer.startAppiumServerAndGetURL());
			generateAppiumDriverSession(url, capabilities);
			deviceInfoObj.setDeviceUsed(true);
			wait = new WebDriverWait(androidDriver, 5);

			LOG.info("PUSH ADB CONTROLLER OBJECT TO ADBFACTORY");
			LOG.info("PUSH APPIUMSERVER OBJECT TO APPIUMSERVERFACTORY");
			AppiumServerFactory.setInvokeAppiumServer(invokeAppiumServer);
		} catch (MalformedURLException mfue) {
			mfue.printStackTrace();
		}

	}

	private AndroidDriver<WebElement> generateAppiumDriverSession(URL url, DesiredCapabilities capabilities)
			throws Exception {
		androidDriver = AppiumSessionFactory.getAndroidDriverSession(url, capabilities);
		wait = new WebDriverWait(androidDriver, 5);
		return androidDriver;
	}

	@Override
	public void quit(HashMap<String, String> params) {

	}

	@Override
	public void launchApp(HashMap<String, String> params) throws Exception {
		if (params.containsKey(Constants.APP_PACKAGE)) {
			String appPath = (String) params.get(Constants.APP_PACKAGE);
			AdbFactory.getAdbController().launchApp(appPath);
		} else {
			// install using other methods
		}

	}

	@Override
	public void click(HashMap<String, String> params) throws Exception {
		if (params.get("byPosition") != null) {
			// performing click by position
			int x = Integer.parseInt(params.get("xPosition"));
			int y = Integer.parseInt(params.get("yPosition"));
			TouchAction ta = new TouchAction(androidDriver);
			ta.tap(PointOption.point(x, y)).perform();
			// ta.tap(x, y).perform();
		} else {
			WebElement element;
			try {
				element = findElement(params);
				element.click();
			} catch (org.openqa.selenium.StaleElementReferenceException e) {
				LOG.info("Error while clicking: " + e.getMessage());
				element = findElement(params);
				if (element.isDisplayed())
					element.click();
			}
		}
	}

	/**
	 * Find element first by using WebDriver APIs. If not found by WebDriver and
	 * searchByAndroid is provided in the params, it will use UiAutomator to search
	 * the element.
	 * 
	 * @param params HashMap
	 * @return WebElement
	 * @throws Exception Throws exception if element not found.
	 */
	private WebElement findElement(HashMap<String, String> params) throws Exception {
		WebElement element = waitForVisibleElement(getByFromParams(params), Integer.parseInt(params.get("timeout")),
				"true");
		if ((null == element) && (null != params.get("searchByAndroid"))) {
			element = findElementWithAndroidDriver(getSelectorStringFromParams(params),
					Integer.parseInt(params.get("timeout")));
			if (null == element) {
				throw new Exception("Element not found.");
			}
		}
		return element;
	}

	private By getByFromParams(HashMap<String, String> params) throws Exception {
		By by = null;
		switch (params.get("searchBy")) {
		case "text":
			by = By.xpath("//*[@text='" + params.get("searchValue") + "']");
			break;
		case "partialtext":
			by = By.xpath("//*[contains(@text,'" + params.get("searchValue") + "')]");
			break;
		case "id":
			by = By.id(params.get("searchValue"));
			break;
		case "content-desc":
			by = By.xpath("//*[@content-desc='" + params.get("searchValue") + "']");
			break;
		case "password":
			by = By.xpath("//*[@password='" + params.get("searchValue") + "']");
			break;
		case "xpath":
			by = By.xpath(params.get("searchValue"));
			break;
		case "type":
			by = By.xpath("//*[@type='" + params.get("searchValue") + "']");
			break;
		case "partialId":
			by = By.xpath("//*[contains(@id,'" + params.get("searchValue") + "')]");
			break;
		case "class":
			by = By.className(params.get("searchValue"));
			break;
		default:
			throw new Exception("Incorrect identifier!");
		}
		return by;

	}

	private String getSelectorStringFromParams(HashMap<String, String> params) {
		String using = null;
		switch (params.get("searchByAndroid")) {
		case "androidPartialId":
			using = "new UiSelector().resourceIdMatches(\"" + params.get("searchValue") + "\")";
			break;
		case "androidClassName":
			using = "new UiSelector().className(\"" + params.get("searchValue") + "\")";
			break;
		default:
			LOG.info("No selector for AndroidDriver.");
		}
		return using;
	}

	private WebElement waitForVisibleElement(final By by, int waitTime, String visibility) {
		WebElement element = null;
		LOG.debug("By= " + by.toString());
		for (int attempt = 0; attempt < waitTime; attempt++) {
			androidDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			try {
				element = androidDriver.findElement(by);
				if (visibility.equals("true")) {
					break;
				} else if (visibility.equals("false")) {
					LOG.debug("Started waiting for element to disappear...");
					long startTime = System.currentTimeMillis();
					Wait<AndroidDriver<WebElement>> customFluentWait = new FluentWait<AndroidDriver<WebElement>>(
							androidDriver).withTimeout(waitTime, TimeUnit.SECONDS).pollingEvery(1, TimeUnit.SECONDS);
					customFluentWait.until(ExpectedConditions.invisibilityOfElementLocated(by));
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

		if (visibility.equals("true")) {
			int count = 0, maxAttempt = 10;
			do {
				count++;
				if (count > maxAttempt)
					break;
				if (element != null) {
					wait.until(ExpectedConditions.visibilityOfElementLocated(by));
				}
			} while (element != null && !element.isDisplayed());
		} else {
			if (element != null) {
				wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
				element = null;
			}
		}

		return element;
	}

	private WebElement findElementWithAndroidDriver(final String using, int time) {
		WebElement element = null;
		LOG.info("By= " + using);
		for (int attempt = 0; attempt < time; attempt++) {
			androidDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			try {
				element = androidDriver.findElementByAndroidUIAutomator(using);
			} catch (Exception exc) {
				LOG.debug("Can't find the element using the AndroidDriver - " + exc.getMessage());
			}
		}
		return element;
	}

	@Override
	public Boolean waitForElement(HashMap<String, String> params) {
		try {
			final By by = getByFromParams(params);
			int waitTime = Integer.parseInt(params.get("timeout"));
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

	@Override
	public void uninstallApplication(HashMap<String, String> params) {
		String bundleId = params.get("BundleId");
		androidDriver.removeApp(bundleId);
	}

	@Override
	public void sendSpecialKeys(String key) {
		Integer keyEvent = Integer.parseInt(key);
		androidDriver.pressKeyCode(keyEvent);
	}

	@Override
	public void changeDriverType(String receivedMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeApplication(HashMap<String, String> params) throws Exception {
		if (params.containsKey(Constants.APP_PACKAGE)) {
			String appPath = params.get(Constants.APP_PACKAGE);
			AdbFactory.getAdbController().closeApplication(appPath);
		}
	}

	@Override
	public String getText(HashMap<String, String> params) {
		try {
			if (!isElementPresent(params))
				return null;
			return findElement(params).getText();
		} catch (Exception e) {
			return null;
		}
	}

	private void scroll(int direction, int x, int y, int dx, int dy) {
		this.hideKeyBoard();
		try {
			if ((direction == Constants.SCROLL_DIRECTION_LEFT && dx > 0)
					|| (direction == Constants.SCROLL_DIRECTION_RIGHT && dx < 0)) {
				dx = -dx;
			}

			if ((direction == Constants.SCROLL_DIRECTION_UP && dy > 0)
					|| (direction == Constants.SCROLL_DIRECTION_DOWN && dy < 0)) {
				dy = -dy;
			}

			Point source = new Point(x, y);
			Point destination = new Point(x + dx, y + dy);
			scrollWithAbsoluteOrRelativeCoordinates(source, destination, IS_SCROLL_ABSOLUTE);
		} catch (Exception e) {
			LOG.debug("Error while swiping: " + e.getMessage());
			Dimension dimensions = androidDriver.manage().window().getSize();
			int height = dimensions.getHeight();
			int width = dimensions.getWidth();
			double scrollStartX = width * 0.5;
			double scrollEndX = width * 0.5;
			double scrollStartY = height * 0.5;
			double scrollEndY = height * 0.5;
			switch (direction) {
			case Constants.SCROLL_DIRECTION_LEFT:
				scrollEndX = width * Constants.SCREEN_LENGTH_MULTIPLIER_20_PERCENT;
				break;

			case Constants.SCROLL_DIRECTION_UP:
				scrollEndY = height * Constants.SCREEN_LENGTH_MULTIPLIER_20_PERCENT;
				break;

			case Constants.SCROLL_DIRECTION_RIGHT:
				scrollEndX = width * Constants.SCREEN_LENGTH_MULTIPLIER_80_PERCENT;
				break;

			case Constants.SCROLL_DIRECTION_DOWN:
				scrollEndY = height * Constants.SCREEN_LENGTH_MULTIPLIER_80_PERCENT;
				break;
			}

			int startX = (int) scrollStartX;
			int startY = (int) scrollStartY;
			int endX = (int) scrollEndX;
			int endY = (int) scrollEndY;
			Point source = new Point(startX, startY);
			Point destination = new Point(endX, endY);
			scrollWithAbsoluteOrRelativeCoordinates(source, destination, IS_SCROLL_ABSOLUTE);
		}
	}

	private void scrollWithAbsoluteOrRelativeCoordinates(Point source, Point destination, boolean isAbsolute) {
		TouchAction ta = new TouchAction(androidDriver);
		Duration duration = Duration.ofMillis(1500);

		if (isAbsolute) {
			ta.press(PointOption.point(source.getX(), source.getY())).waitAction(WaitOptions.waitOptions(duration))
					.moveTo(PointOption.point(destination.getX(), destination.getY())).release().perform();
			// ta.press(source.getX(),
			// source.getY()).waitAction(duration).moveTo(destination.getX() ,
			// destination.getY()).release().perform();
		} else {
			int diffX = (int) (destination.getX() - source.getX());
			int diffY = (int) (destination.getY() - source.getY());
			// ta.press(source.getX(), source.getY()).waitAction(duration).moveTo(diffX,
			// diffY).release().perform();
			ta.press(PointOption.point(source.getX(), source.getY())).waitAction(WaitOptions.waitOptions(duration))
					.moveTo(PointOption.point(diffX, diffY)).release().perform();
		}
	}

	private void resolveScrollType(int direction, HashMap<String, String> params) throws Exception {
		if (null != params.get("searchBy")) {
			scrollByElementSize(direction, findElement(params));
		} else {
			String x = params.get("startPositionX");
			String y = params.get("startPositionY");
			String diffX = params.get("diffX");
			String diffY = params.get("diffY");

			scroll(direction, null != x ? Integer.parseInt(x) : Constants.SCROLL_INVALID_VALUE,
					null != y ? Integer.parseInt(y) : Constants.SCROLL_INVALID_VALUE,
					null != diffX ? Integer.parseInt(diffX) : Constants.SCROLL_INVALID_VALUE,
					null != diffY ? Integer.parseInt(diffY) : Constants.SCROLL_INVALID_VALUE);
		}
	}

	private void scrollByElementSize(int direction, WebElement element) {
		int offset = 0;
		Point location = element.getLocation();
		Dimension size = element.getSize();
		Point center = new Point(location.getX() + size.getWidth() / 2, location.getY() + size.getHeight() / 2);
		Point destination = null;
		switch (direction) {
		case Constants.SCROLL_DIRECTION_UP:
			offset = size.getHeight() / 2;
			destination = new Point(center.getX(), center.getY() - offset);
			break;

		case Constants.SCROLL_DIRECTION_DOWN:
			offset = size.getHeight() / 2;
			destination = new Point(center.getX(), center.getY() + offset);
			break;

		case Constants.SCROLL_DIRECTION_LEFT:
			offset = size.getWidth() / 2;
			destination = new Point(center.getX() - offset, center.getY());
			break;

		case Constants.SCROLL_DIRECTION_RIGHT:
			offset = size.getWidth() / 2;
			destination = new Point(center.getX() + offset, center.getY());
			break;
		}
		scrollWithAbsoluteOrRelativeCoordinates(center, destination, IS_SCROLL_ABSOLUTE);
	}

	/**
	 * Scrolls up from center of screen for 30% of screen if no params were set.
	 * Same if only startPositionX or only startPositionY are specified. To set
	 * start point of scroll correctly both startPositionX and startPositionY should
	 * be specified. Scroll end point calculated with diffX and diffY in next way:
	 * end point x = startPointX + diffX. Same for end point y. If params contains
	 * element to search for - scroll will be performed with length equals to height
	 * of specified element.
	 * 
	 * @param params HashMap<String, String> with next keys - values: (optional)
	 *               startPositionX - any int (optional) startPositionY - any int
	 *               (optional) diffX - any int (optional) diffY - any int OR
	 *               scrollBy - search identifier (id, text...) searchValue - search
	 *               value timeout - seconds in format "5", "2"...
	 */
	@Override
	public void scrollDown(HashMap<String, String> params) throws Exception {
		resolveScrollType(Constants.SCROLL_DIRECTION_UP, params);
	}

	/**
	 * Same as {@link ToolHelperFactory#scrollUp(HashMap)}, but scroll down.
	 */
	@Override
	public void scrollUp(HashMap<String, String> params) throws Exception {
		resolveScrollType(Constants.SCROLL_DIRECTION_DOWN, params);
	}

	@Override
	public void scrollTo(HashMap<String, String> params) {
		hideKeyBoard();
		String xpath = "//*[@text()='" + params.get("scrollToText") + "']";
		List<WebElement> elements = androidDriver.findElements(By.xpath(xpath));
		int scrollCounter = 0;
		final int MAX_SCROLL_ATTEMPTS = 5;
		WebElement element = null;
		try {
			File currentScreen = androidDriver.getScreenshotAs(OutputType.FILE);
			Region screenRegion = this.getRegionForScreen(currentScreen);
			Integer starX = screenRegion.getW() / 2;
			Integer startY = screenRegion.getH() / 2;
			Integer diffX = starX - 1;
			Integer diffY = startY - 1;
			params.put("startPositionX", starX.toString());
			params.put("startPositionY", startY.toString());
			params.put("diffX", diffX.toString());
			params.put("diffY", diffY.toString());

			while (elements.isEmpty() && scrollCounter < MAX_SCROLL_ATTEMPTS) {
				params.remove("searchBy");
				this.resolveScrollType(Constants.SCROLL_DIRECTION_UP, params);
				elements = androidDriver.findElements(By.xpath(xpath));
				if (elements.size() <= 0) {
					params.put("searchValue", params.get("scrollToText"));
					params.put("searchBy", "text");
					params.put("timeout", "30");
					try {
						element = this.findElement(params);
					} catch (Exception e) {
						element = null;
					}
					if (element != null && element.isDisplayed()) {
						elements.add(0, element);
					}
				}
				scrollCounter++;
			}

			scrollCounter = -scrollCounter;

			if (elements.isEmpty()) {
				while (elements.isEmpty() && scrollCounter < MAX_SCROLL_ATTEMPTS) {
					params.remove("searchBy");
					this.resolveScrollType(Constants.SCROLL_DIRECTION_DOWN, params);
					elements = androidDriver.findElements(By.xpath(xpath));
					if (elements.size() <= 0) {
						params.put("searchValue", params.get("scrollToText"));
						params.put("searchBy", "text");
						params.put("timeout", "30");
						try {
							element = this.findElement(params);
						} catch (Exception e) {
							element = null;
						}
						if (element != null && element.isDisplayed()) {
							elements.add(0, element);
						}
					}
					scrollCounter++;
				}
			}

			if (elements.isEmpty() || !elements.get(0).isDisplayed()) {
				LOG.debug("Cannot scroll to the element.");
			}
		} catch (Exception ex) {
			LOG.debug("Exception during scrollTo: " + ex.getMessage());
		}

	}

	@SuppressWarnings("static-access")
	private Region getRegionForScreen(File currentScreen) throws Exception {
		IIORegistry registry = IIORegistry.getDefaultInstance();
		registry.getServiceProviderByClass(javax.imageio.ImageIO.class).scanForPlugins();
		BufferedImage bimg = registry.getServiceProviderByClass(javax.imageio.ImageIO.class).read(currentScreen);
		return new Region(0, 0, bimg.getWidth(), bimg.getHeight());
	}

	@Override
	public void longPress(HashMap<String, String> params) throws Exception {
		WebElement ele = findElement(params);

		String originalContext = androidDriver.getContext();
		androidDriver.context("NATIVE_APP");

		TouchAction action = new TouchAction(androidDriver);
		try {
			// action.longPress(element).release().perform();
			action.longPress(new LongPressOptions().withElement(ElementOption.element(ele))
					.withDuration(Duration.ofMillis(1000))).release().perform();
		} catch (WebDriverException e) {
			LOG.debug("Error while longpressing", e);
			androidDriver.context(originalContext);
			longPress(params);
		}
		androidDriver.context(originalContext);
	}

	@Override
	public Boolean isElementPresent(HashMap<String, String> params) throws Exception {
		try {
			return androidDriver.findElements(getByFromParams(params)).size() > 0
					&& androidDriver.findElement(getByFromParams(params)).isDisplayed();
		} catch (NoSuchElementException ex) {
			return false;
		} catch (StaleElementReferenceException sere) {
			return androidDriver.findElements(getByFromParams(params)).size() > 0
					&& androidDriver.findElement(getByFromParams(params)).isDisplayed();
		} catch (Exception ex) {
			LOG.debug("Exception in isElementPresent: " + ex.getMessage());
			ex.printStackTrace();
			return false;
		}

	}

	@Override
	public void hideKeyBoard() {
		try {
			androidDriver.hideKeyboard();
		} catch (Exception e) {
			LOG.debug("Soft keyboard already hidden: " + e.getMessage());
		}
	}

	@Override
	public Boolean isKeyboardShown() {
		return androidDriver.isKeyboardShown();
	}

	@Override
	public Boolean installApplication(HashMap<String, String> params) {
		String appDetails = params.get("AppPath");

		if (this.deviceInfoObj.isRemoteDevice() && new File(appDetails).exists()) {
			// The Execution is Remote Device Execution and Test Resource exists in Local
			// Repo.
			// It has to be copied first and change the path accordingly.
			String remoteApkFilePath = this.ittDriverContext
					.getAttribute(ITTDriverConstants.TEST_RESOURCES_TEMP_FILE_DIR_KEY).toString()
					+ ITTDriverConstants.FILE_SEPARATOR + new File(appDetails).getName();
			this.sshClientObj.SCPTo(appDetails, remoteApkFilePath);
			if (!this.sshClientObj.isFileExists(remoteApkFilePath)) {
				return false;
			}
			appDetails = remoteApkFilePath;
		}
		androidDriver.installApp(appDetails);
		return true;
	}

	@Override
	public void unlockDevice(HashMap<String, String> params) throws Exception {
		LOG.info("----AppiumTestDriver.wakeupDevice()------");
		LOG.info("password" + params.get("password"));
		params.put("searchBy", "text");
		params.put("searchValue", "Confirm password");
		params.put("timeout", "5");
		if (isElementPresent(params)) {
			AdbFactory.getAdbController().unlockdevice((String) params.get("password"));
		} else {
			params.put("searchValue", "Select screen lock");
			if (isElementPresent(params)) {
				params.put("searchBy", "text");
				params.put("searchValue", "None");
				params.put("timeout", "5");
				click(params);
			} else {
				throw new Exception("Neither asked for password not went to" + " the Select Screen Lock page!!!");
			}
		}
	}

	@Override
	public void wakeUpDevice() throws Exception {
		AdbFactory.getAdbController().wakeUpDevice();
	}

	@Override
	public void pressHome() {
		androidDriver.pressKeyCode(3);
	}

	public void pressBackButton() {
		androidDriver.navigate().back();
	}

	@Override
	public void swipeLeft(HashMap<String, String> params) throws Exception {
		resolveScrollType(Constants.SCROLL_DIRECTION_LEFT, params);
	}

	@Override
	public void swipeRight(HashMap<String, String> params) throws Exception {
		resolveScrollType(Constants.SCROLL_DIRECTION_RIGHT, params);
	}

	@Override
	public void longswipeRight(HashMap<String, String> params) throws Exception {
		resolveScrollType(Constants.SCROLL_DIRECTION_RIGHT, params);
	}

	@Override
	public void clearApplicationData(HashMap<String, String> params) throws Exception {
		if (params.containsKey(Constants.APP_PACKAGE)) {
			AdbFactory.getAdbController().clearAppCache(params.get(Constants.APP_PACKAGE).toString());
		} else if (params.containsKey(Constants.PARAMS_APP_NAME)) {
			// implement clear app cache for appname through gui
		} else {
			throw new Exception("Neither application name not application path provided!!!");
		}

	}

	@Override
	public String getElementProperty(HashMap<String, String> params) throws Exception {
		WebElement element = findElement(params);
		String propertyName = params.get("propertyName");
		return element.getAttribute(propertyName);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List findElements(HashMap<String, String> params) throws Exception {
		return androidDriver.findElements(getByFromParams(params));
	}

	@Override
	public Boolean verifyElementProperty(HashMap<String, String> params) throws Exception {
		String propertyValue = params.get("propertyValue");
		return getElementProperty(params).equals(propertyValue);
	}

	@Override
	public Boolean isApplicationInstalled(HashMap<String, String> params) {
		try {
			String appPackage = params.get(Constants.APP_PACKAGE);
			return AdbFactory.getAdbController().isPackageInstalled(appPackage);
		} catch (Exception e) {
			LOG.debug("Unable to fetch the app package");
			return null;
		}
	}

	@Override
	public void takeDeviceScreenShot(HashMap<String, String> params) throws Exception {
		Path srcFile = androidDriver.getScreenshotAs(OutputType.FILE).toPath();
		// Wait for 10 seconds to populate the screenshot.
		Thread.sleep(10000);
		Path target = new File(params.get("FilePath") + ".png").toPath();
		try {
			Files.copy(srcFile, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void openNotificationTray() {
		String openNotificationCommand = " shell service call statusbar 1";
		LOG.info("OPEN NOTIFICATION CENTER");
		AdbFactory.getAdbController().executeAdbCommand(openNotificationCommand);
	}

	@Override
	public void startLogging(String filePath) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void stopLogging() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void rebootDevice() {
		// TODO Auto-generated method stub
	}

	@Override
	public void lockDevice() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dragDown(HashMap<String, String> params) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void changeDeviceOrientation(HashMap<String, String> params) throws Exception {
		String propertyName = params.get("screenOrientation");
		switch (propertyName) {
		case "landscape":
			androidDriver.rotate(ScreenOrientation.LANDSCAPE);
			break;
		case "portrait":
			androidDriver.rotate(ScreenOrientation.PORTRAIT);
			break;
		}
	}

	@Override
	public void setPickerValues(HashMap<String, String> params) throws Exception {

	}

	@Override
	public void setLocation(HashMap<String, String> params) throws Exception {

	}

	@Override
	public void clearLocation() {

	}

	@Override
	public String getCurrentApplication() {
		return null;
	}

	@Override
	public void setProperty(HashMap<String, String> params) throws Exception {

	}

	@Override
	public WebElement findElement(By by) {
		return androidDriver.findElement(by);
	}

	@Override
	public void tap(HashMap<String, String> params) throws Exception {
		WebElement element;
		try {
			element = findElement(params);
			TouchAction ta = new TouchAction(androidDriver);
			ta.tap(PointOption.point(element.getLocation().getX(), element.getLocation().getY())).perform();
			// ta.tap(element.getLocation().getX(), element.getLocation().getY()).perform();
		} catch (org.openqa.selenium.StaleElementReferenceException e) {
			LOG.info("Error while clicking: " + e.getMessage());
			element = findElement(params);
			if (element.isDisplayed())
				element.click();
		}
	}

	@Override
	public void switchTo(HashMap<String, String> params) throws Exception {
		String type = params.get("type");
		String nameOrHandle = params.get("nameOrHandle");
		if (type.equalsIgnoreCase("window")) {
			androidDriver.switchTo().window(nameOrHandle);
		} else if (type.equalsIgnoreCase("frame")) {
			if (!StringUtils.isEmpty(params.get("searchBy"))) {
				By by = getByFromParams(params);
				WebElement element = androidDriver.findElement(by);
				androidDriver.switchTo().frame(element);
			} else if (params.containsKey("index")) {
				String index = params.get("index");
				Integer indexInt = Integer.parseInt(index);
				androidDriver.switchTo().frame(indexInt);
			} else {
				androidDriver.switchTo().frame(nameOrHandle);
			}
		}
	}

	@Override
	public String getAttributeValue(HashMap<String, String> params) throws Exception {
		return null;
	}

	@Override
	public void swipeOrScrollByCoordinates(HashMap<String, String> params) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAppinForeGround(HashMap<String, String> params) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAppKilled(HashMap<String, String> params) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAppinBackGround(HashMap<String, String> params) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void executeNativeCommand(String command, HashMap<String, Object> params) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPageSource() throws Exception {
		return this.androidDriver.getPageSource();
	}

	@Override
	public void startRecordingScreen(int timeout) throws Exception {
		((CanRecordScreen) this.androidDriver).startRecordingScreen(
				new AndroidStartScreenRecordingOptions().enableBugReport().withTimeLimit(Duration.ofMinutes(10)));

	}

	@Override
	public void stopRecordingScreen(String filePath) throws Exception {
		String screenRecordfilePath = System.getProperty("user.dir") + File.separator + "target" + File.separator
				+ "DeviceScreenRecords" + File.separator + filePath + ".mp4";
		if (!new File(screenRecordfilePath).getParentFile().exists()) {
			new File(screenRecordfilePath).getParentFile().mkdir();
		}
		String base64String = ((CanRecordScreen) this.androidDriver).stopRecordingScreen();
		byte[] data = Base64.decodeBase64(base64String);
		Path path = Paths.get(screenRecordfilePath);
		Files.write(path, data);

	}

	public <T> T getDeviceController() throws Exception {
		// TODO Auto-generated method stub
		return (T) this.adbController;
	}

	@Override
	public ITTDriverContext getDriverContext() throws Exception {
		// TODO Auto-generated method stub
		return this.ittDriverContext;
	}

	
	@Override
	public void sendValue(HashMap<String, String> params) throws Exception {

		WebElement element = findElement(params);
		boolean disableClear = params.get("noClear") != null;
		boolean useAdb = params.get("withAdb") != null;
		String value = params.get("enterValue"); // in this case the value will be text

		if (disableClear) {
			element.click();
		} else {
			element.clear();
		}

		if (useAdb) {
			AdbFactory.getAdbController().sendText(value);
		} else {
			element.sendKeys(value);
		}

	}

	@Override
	public void navigateToUrl(String url) throws Exception {
		this.androidDriver.get(url);
		
	}

	@Override
	public void refreshPage() throws Exception {
		this.androidDriver.navigate().refresh();
		
	}

}
