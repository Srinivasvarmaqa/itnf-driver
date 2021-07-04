package com.itt.factoryhelper;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itt.common.BrowserInfo;
import com.itt.context.ITTDriverContext;

public class EdgeBrowserDriver extends BrowserHelperFactory {
	private static final Logger LOG = LoggerFactory
			.getLogger(EdgeBrowserDriver.class);

	private WebDriver driver;
	private BrowserInfo browserInfoObj;
	private ITTDriverContext ittDriverContext;
	static String USER_DIR = System.getProperty("user.dir");
	static String FILE_SEPARATOR = System.getProperty("file.separator");

	public EdgeBrowserDriver(BrowserInfo browserInfo) throws Exception {
		LOG.info("EDGE BROWSER DRIVER");
		this.browserInfoObj = browserInfo;
		this.ittDriverContext = new ITTDriverContext();
	}

	public synchronized void invokeEdgeDriver() throws Exception {
		DesiredCapabilities capabilities;
		capabilities = DesiredCapabilities.edge();
		capabilities.setPlatform(Platform.LINUX);
		capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,UnexpectedAlertBehaviour.ACCEPT_AND_NOTIFY);
		Capabilities cap;
	
		if (browserInfoObj.isRemoteDriver()) {
			LOG.info("LAUNCH EDGE REMOTE WEB DRIVER");
			URL huburl = new URL(this.browserInfoObj.getSeleniumHubURL());
			this.driver = new RemoteWebDriver(huburl, capabilities);
			cap = ((RemoteWebDriver) this.driver).getCapabilities();
		} else {
			LOG.info("LAUNCH WEB DRIVER STANDLONE SERVER");
			String edgeDriverPath = System.getProperty("EDGE_LOCAL_DRIVER_PATH");
			System.setProperty("webdriver.edge.driver", edgeDriverPath);
			this.driver = new EdgeDriver();
			cap = ((EdgeDriver) this.driver).getCapabilities();
		}
		this.browserInfoObj.setBrowserVersion(cap.getVersion().toString());
		this.ittDriverContext.setAttribute("PARENT_WINDOW_HANDLE_ID", this.driver.getWindowHandle());
		this.driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
//		this.driver.manage().window().maximize();
	}

	public void invokeDriver() throws Exception {
		this.invokeEdgeDriver();
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
