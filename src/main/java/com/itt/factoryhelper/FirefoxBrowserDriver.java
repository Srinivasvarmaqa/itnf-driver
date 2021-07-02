package com.itt.factoryhelper;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itt.common.BrowserInfo;
import com.itt.context.ITTDriverContext;

public class FirefoxBrowserDriver extends BrowserHelperFactory {
	private static final Logger LOG = LoggerFactory.getLogger(FirefoxBrowserDriver.class);

	private WebDriver driver;
	private BrowserInfo browserInfoObj;
	private ITTDriverContext ittDriverContext;
	static String USER_DIR = System.getProperty("user.dir");
	static String FILE_SEPARATOR = System.getProperty("file.separator");

	public FirefoxBrowserDriver(BrowserInfo browserInfo) throws Exception {
		LOG.info("FIREFOX BROWSER DRIVER");
		this.browserInfoObj = browserInfo;
		this.ittDriverContext = new ITTDriverContext();
	}

	public synchronized void invokeFirefoxDriver() throws Exception {
		DesiredCapabilities capabilities;
		capabilities = DesiredCapabilities.firefox();
		capabilities.setCapability("browserName", "firefox");
		capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,UnexpectedAlertBehaviour.ACCEPT_AND_NOTIFY);
		Capabilities cap;

		if (browserInfoObj.isRemoteDriver()) {
			LOG.info("LAUNCH FIREFOX REMOTE WEB DRIVER");
			URL huburl = new URL(this.browserInfoObj.getSeleniumHubURL());
			this.driver = new RemoteWebDriver(huburl, capabilities);
			cap = ((RemoteWebDriver) this.driver).getCapabilities();
		} else {
			LOG.info("LAUNCH WEB DRIVER STANDLONE SERVER");
			String firefoxDriverPath = System.getProperty("FIREFOX_LOCAL_DRIVER_PATH");
			System.setProperty("webdriver.firefox.driver", firefoxDriverPath);
			this.driver = new FirefoxDriver();
			cap = ((FirefoxDriver) this.driver).getCapabilities();
		}
		this.browserInfoObj.setBrowserVersion(cap.getVersion().toString());
		this.ittDriverContext.setAttribute("PARENT_WINDOW_HANDLE_ID", this.driver.getWindowHandle());
		this.driver.manage().timeouts().setScriptTimeout(20, TimeUnit.SECONDS);
		this.driver.manage().window().maximize();

	}

	public void invokeDriver() throws Exception {
		this.invokeFirefoxDriver();
	}

	@Override
	public WebDriver getWebDriver() throws Exception {
		return this.driver;
	}

	@Override
	public ITTDriverContext getDriverContext() throws Exception {
		return this.ittDriverContext;
	}
}
