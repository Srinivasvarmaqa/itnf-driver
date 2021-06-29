package com.itt.common;

public class DeviceInfo {

	private String osVersion;
	private String modelName;
	private String deviceID;
	private String rawInfo;
	private String tafInstanceName;
	private String testRailRunId;
	private int screenWidth;
	private int screenHeight;
	private boolean deviceUsed = false;
	private boolean isConnected = false;
	private String deviceName;
	private String customWdaUrl;
	private String xcodeProject;
	private String appiumSourcePath;
	private String platformName;

	private String loginUserName;
	private String iPAddress;
	private String loginPassword;

	private String appiumServerURL;
	private String executionRunId;

	public String getExecutionRunId() {
		return executionRunId;
	}

	public void setExecutionRunId(String executionRunId) {
		this.executionRunId = executionRunId;
	}

	private String browser;

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public String getPlatformName() {
		return platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}

	public String getAppiumSourcePath() {
		return appiumSourcePath;
	}

	public void setAppiumSourcePath(String appiumSourcePath) {
		this.appiumSourcePath = appiumSourcePath;
	}

	public String getXcodeProject() {
		return xcodeProject;
	}

	public void setXcodeProject(String xcodeProject) {
		this.xcodeProject = xcodeProject;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getOsVersion() {
		return this.osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getModelName() {
		return this.modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getRawInfo() {
		return this.rawInfo;
	}

	public void setRawInfo(String rawInfo) {
		this.rawInfo = rawInfo;
	}

	public String getTafInstanceName() {
		return tafInstanceName;
	}

	public String getTestRailRunId() {
		return testRailRunId;
	}

	public void setTafInstanceName(String appInstanceName) {
		this.tafInstanceName = appInstanceName;
	}

	public String getDeviceID() {
		return this.deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public boolean isDeviceUsed() {
		return this.deviceUsed;
	}

	public void setDeviceUsed(boolean deviceUsed) {
		this.deviceUsed = deviceUsed;
	}

	public boolean isConnected() {
		return this.isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public void setTestRailRunId(String testRailRunId) {
		this.testRailRunId = testRailRunId;
	}

	public String getCustomWdaUrl() {
		return customWdaUrl;
	}

	public void setCustomWdaUrl(String customWdaUrl) {
		this.customWdaUrl = customWdaUrl;
	}

	public String getLoginUserName() {
		return loginUserName;
	}

	public void setLoginUserName(String loginUserName) {
		this.loginUserName = loginUserName;
	}

	public String getiPAddress() {
		return iPAddress;
	}

	public void setiPAddress(String iPAddress) {
		this.iPAddress = iPAddress;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}

	/**
	 * @return the appiumServerURL
	 */
	public String getAppiumServerURL() {
		return appiumServerURL;
	}

	/**
	 * @param appiumServerURL the appiumServerURL to set
	 */
	public void setAppiumServerURL(String appiumServerURL) {
		this.appiumServerURL = appiumServerURL;
	}

	/**
	 * @return True if the device is connected to Remote Hub
	 */
	public boolean isRemoteDevice() {
		return this.getiPAddress() != null && this.getiPAddress().length() > 0 && this.getLoginUserName() != null
				&& this.getLoginPassword() != null && this.getLoginUserName().length() > 0
				&& this.getLoginPassword().length() > 0;
	}

	@Override
	public String toString() {
		return "Device ID : " + this.deviceID + "  Model Name: " + this.modelName + "  OSVersion: " + this.osVersion
				+ "  DeviceUsed: " + this.deviceUsed + "  appInstanceName: " + this.tafInstanceName
				+ "  testRailRunId: " + this.testRailRunId + "  iPAddress: " + this.iPAddress + "  loginUserName: "
				+ this.loginUserName + "  loginPassword: " + this.loginPassword + " appiumServerURL: "
				+ this.appiumServerURL;
	}

}
