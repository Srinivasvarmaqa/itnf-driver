package com.itt.common;

public class BrowserInfo {

	private String browser;
	private String browserVersion;
	private String hubUrl;

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}
	
	public String getBrowserVersion() {
		return browserVersion;
	}

	public void setBrowserVersion(String browserVersion) {
		this.browserVersion = browserVersion;
	}

	public String getSeleniumHubURL() {
		return hubUrl;
	}

	public void setSeleniumHubURL(String url) {
		this.hubUrl = url;
	}

	/**
	 * @return True if the device is connected to remote driver
	 */
	
	public boolean isRemoteDriver() {
		try {
			return this.getSeleniumHubURL() != null;
		} catch (Exception e) {
			return false;
		}
	}
}
