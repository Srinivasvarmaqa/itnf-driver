package com.itt.factoryhelper;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itt.common.BrowserInfo;
import com.itt.context.ITTDriverContext;

public class ChromeBrowserDriver extends BrowserHelperFactory {
	private static final Logger LOG = LoggerFactory.getLogger(ChromeBrowserDriver.class);

	private WebDriver driver;
	private BrowserInfo browserInfoObj;
	private ITTDriverContext ittDriverContext;
	static String USER_DIR = System.getProperty("user.dir");
	static String FILE_SEPARATOR = System.getProperty("file.separator");

	public ChromeBrowserDriver(BrowserInfo browserInfo) throws Exception {
		LOG.info("CHROME BROWSER DRIVER");
		this.browserInfoObj = browserInfo;
		this.ittDriverContext = new ITTDriverContext();
	}

	public synchronized void invokeChromeDriver() throws Exception {
		DesiredCapabilities capabilities;
		capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability("browserName", "chrome");
		capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,
				UnexpectedAlertBehaviour.IGNORE);

		Capabilities cap;
		
		if (browserInfoObj.isRemoteDriver()) {
			LOG.info("LAUNCH CHROME REMOTE WEB DRIVER");
			URL huburl = new URL(this.browserInfoObj.getSeleniumHubURL());
			this.driver = new RemoteWebDriver(huburl, capabilities);
			cap = ((RemoteWebDriver) this.driver).getCapabilities();
		} else {
			LOG.info("LAUNCH WEB DRIVER STANDLONE SERVER");
			ChromeOptions options = new ChromeOptions();
			String chromeDriverPath = System.getProperty("CHROME_LOCAL_DRIVER_PATH");
			if (chromeDriverPath == null) {
				LOG.error("Chrome local Driver path is not provided");
				throw new Exception("Chrome local driver path is not set, set Env variable CHROME_LOCAL_DRIVER_PATH");
			}
			System.setProperty("webdriver.chrome.driver", chromeDriverPath);
			this.driver = new ChromeDriver(options);
			cap = ((ChromeDriver) this.driver).getCapabilities();
		}
		this.browserInfoObj.setBrowserVersion(cap.getVersion().toString());
		this.ittDriverContext.setAttribute("PARENT_WINDOW_HANDLE_ID", this.driver.getWindowHandle());
		this.driver.manage().timeouts().setScriptTimeout(20, TimeUnit.SECONDS);
		this.driver.manage().window().maximize();
	}

	public void invokeDriver() throws Exception {
		this.invokeChromeDriver();
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
