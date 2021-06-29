package com.itt.appium;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itt.common.DeviceInfo;
import com.itt.common.Utils;
import com.itt.context.ITTDriverConstants;
import com.itt.ssh.SshClient;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Random;

public class InvokeAppiumServer {

	private int[] ports = new int[] { 4721, 4722, 4723, 4724, 4725, 4726, 4727, 4728, 4729, 4730, 4731, 4732, 4733,
			4734, 4735, 4736, 4737, 4738, 4739, 4740, 4741, 4742, 4743, 4744, 4745, 4746, 4747, 4748, 4749, 4750, 4751,
			4752, 4753, 4754, 4755 };
	private String hostName;
	private String nodePath = System.getenv("NODE");
	private String appiumMainJS = System.getenv("APPIUM_PATH");
	private String appiumServerUrl;
	private DeviceInfo deviceInfoObj;
	private SshClient sshClientObj;
	private int defaultSocketTimeout = 3;
	private File APPIUM_LOG_TEMPFILDER;
	private File APPIUM_LOG_FILE;

	private String appiumFilePath;
	private Utils commonUtils;
	private static final String COMMON_TEMP_FILE_DIR = "/usr/local/var/log/appium/";
	private final String homeDir = System.getProperty("user.dir") + File.separator + "target";
	Process process;
	private static final Logger LOG = LoggerFactory.getLogger(InvokeAppiumServer.class);

	public InvokeAppiumServer(DeviceInfo deviceInfoObj) throws Exception {
		this.deviceInfoObj = deviceInfoObj;
		int Randomnumber = new Random().nextInt(1000) + 1;
		hostName = "127.0.0.1";
		commonUtils = new Utils();
		if (this.deviceInfoObj.isRemoteDevice()) {
			sshClientObj = new SshClient(this.deviceInfoObj.getiPAddress(), this.deviceInfoObj.getLoginPassword(),
					this.deviceInfoObj.getLoginUserName(), true);
			hostName = deviceInfoObj.getiPAddress();
			String tempRemoteDeviceExecutionFolderPath = String.format(ITTDriverConstants.DEVICE_TEMP_FILE_DIR_PATH,
					this.deviceInfoObj.getExecutionRunId(), deviceInfoObj.getDeviceID());
			String tempRemoteDeviceAppiumLogsFolderPath = tempRemoteDeviceExecutionFolderPath
					+ ITTDriverConstants.FILE_SEPARATOR + ITTDriverConstants.APPIUM_LOGS_FOLDER_NAME;
			APPIUM_LOG_TEMPFILDER = new File(tempRemoteDeviceAppiumLogsFolderPath);
			this.sshClientObj
					.executeRemoteCommand(String.format("mkdir -p %s", APPIUM_LOG_TEMPFILDER.getAbsolutePath()));
			this.APPIUM_LOG_FILE = new File(
					APPIUM_LOG_TEMPFILDER + File.separator + "appiumLogs_" + Randomnumber + ".log");
			appiumFilePath = APPIUM_LOG_FILE.getAbsolutePath();

			if (appiumMainJS == null || nodePath == null) {
				appiumMainJS = "$APPIUM_PATH";
				nodePath = "$NODE ";
			}

		} else {
			appiumFilePath = homeDir + File.separator + "AppiumLogs" + File.separator + "appiumLogs_" + Randomnumber
					+ ".log";
		}

	}

	public InvokeAppiumServer() {

	}

	public String startAppiumServerAndGetURLbyPlatForm(String Platform) throws Exception {
		String command;
		int availablePort = 0;
		LOG.debug("DEVICE PLATFROM TO GET THE APPIUM SERVER URL IS" + Platform.toLowerCase());
		synchronized (this) {
			String appiumArgs = "--allow-insecure chromedriver_autodownload --no-reset --log-level debug --local-timezone --log " + appiumFilePath;
			for (int i = 0; i < ports.length; i++) {
				// Taking the Ports from 4721 to 4744 for Android Devices
				if (Platform.toLowerCase().contains("android") && ports[i] < 4745) {
					if (!this.commonUtils.isPortInUse(hostName, ports[i])) {
						availablePort = ports[i];
						LOG.info("available Port " + availablePort);
						break;
					}
					// Taking the Ports from 4745 to 4755 for iOs Devices
				} else if (Platform.toLowerCase().contains("ios") && ports[i] >= 4745) {
					if (!this.commonUtils.isPortInUse(hostName, ports[i])) {
						availablePort = ports[i];
						LOG.info("available Port " + availablePort);
						break;
					}
				}
			}
			if (availablePort == 0) {
				throw new Exception("Ports are not available");
			} else {
				command = nodePath + "  " + appiumMainJS + " ";
				command = command + " -a " + hostName + " -p " + availablePort + " ";
				command = command + " -cp " + (availablePort + 1000) + " -bp " + (availablePort + 2000);
				command = command + " --chromedriver-port " + (availablePort + 3000);
				command = command + " " + appiumArgs;
				LOG.debug("Command to start Appium Server " + command);
			}

			process = Runtime.getRuntime().exec(command);

			LOG.info("Wait for Appium server to start - Ideally it should not take more than 10 seconds");
			int counter = 0;
			while (!this.commonUtils.isPortInUse(hostName, availablePort)) {
				LOG.info("Appium is not started. Waiting 1 second.");
				Thread.sleep(10 * 1000);
				counter++;
				// Waiting for Appium session to be created for Max 20 mins
				if (counter > 120 && !this.commonUtils.isPortInUse(hostName, availablePort)) {
					// Setting the port 0 as the appium session
					availablePort = 0;
					LOG.info("There is some problem with Creating Appium Session");
					break;
				}

			}
		}
		if (this.deviceInfoObj.isRemoteDevice()) {
			appiumServerUrl = "http://" + this.deviceInfoObj.getiPAddress() + ":" + availablePort + "/wd/hub";
		} else {
			appiumServerUrl = "http://" + hostName + ":" + availablePort + "/wd/hub";

		}
		return appiumServerUrl;

	}

	public String startAppiumServerAndGetURL() throws Exception {
		final String homeDir = System.getProperty("user.dir") + File.separator + "target";
		String command;
		int availablePort = 0;

		if (!StringUtils.isEmpty(this.deviceInfoObj.getAppiumSourcePath())) {
			appiumMainJS = this.deviceInfoObj.getAppiumSourcePath();
		}

		if (appiumMainJS == null || nodePath == null) {
			throw new Exception(" Environment varaiable not found for APPIUM_PATH or NODE ");
		}

		LOG.debug("DEVICE PLATFROM TO GET THE APPIUM SERVER URL IS" + deviceInfoObj.getPlatformName().toLowerCase());

		// Appium Session Recovery is happening in both @Test and @Befor or @After
		// Method levels
		// To avoid Same Port being used by two different devices and to avoid deadlock
		synchronized (this) {

			// Cleanup Appium Process if anything is already running for given Device UDID.
			LOG.debug("CLEANUP THE STALED APPIUM PROCESSES FOR GIVEN DEVICE DETAILS " + deviceInfoObj.toString());
			this.cleanupStaleExistingAppiumProcess();

			String appiumArgs = " --allow-insecure chromedriver_autodownload --no-reset --log-level debug --local-timezone --log " + appiumFilePath;
			for (int i = 0; i < ports.length; i++) {
				// Taking the Ports from 4721 to 4744 for Android Devices
				if (deviceInfoObj.getPlatformName().toLowerCase().contains("android") && ports[i] < 4745) {
					if (!this.commonUtils.isPortInUse(hostName, ports[i])) {
						availablePort = ports[i];
						LOG.info("available Port " + availablePort);
						break;
					}
					// Taking the Ports from 4745 to 4755 for iOs Devices
				} else if (deviceInfoObj.getPlatformName().toLowerCase().contains("ios") && ports[i] >= 4745) {
					if (!this.commonUtils.isPortInUse(hostName, ports[i])) {
						availablePort = ports[i];
						LOG.info("available Port " + availablePort);
						break;
					}
				}
			}

			if (availablePort == 0) {
				throw new Exception("Ports are not available");
			} else {
				command = nodePath + "  " + appiumMainJS + " ";
				command = command + " -a " + hostName + " -p " + availablePort + " ";
				command = command + " -cp " + (availablePort + 1000) + " -bp " + (availablePort + 2000);
				command = command + " --chromedriver-port " + (availablePort + 3000);
				command = command + " --device-name " + this.deviceInfoObj.getDeviceID();
				command = command + " " + appiumArgs;
				LOG.info("Command to start Appium Server " + command);
			}

			if (this.deviceInfoObj.isRemoteDevice()) {
				this.sshClientObj.launchService(command);
			} else {
				process = Runtime.getRuntime().exec(command);
			}

			LOG.info("Wait for Appium server to start - Ideally it should not take more than 10 seconds");
			int counter = 0;
			while (!this.commonUtils.isPortInUse(hostName, availablePort)) {
				LOG.info("Appium is not started. Waiting 1 second.");
				Thread.sleep(10 * 1000);
				counter++;
				// Waiting for Appium session to be created for Max 20 mins
				if (counter > 120 && !this.commonUtils.isPortInUse(hostName, availablePort)) {
					// Setting the port 0 as the appium session
					availablePort = 0;
					LOG.info("There is some problem with Creating Appium Session");
					break;
				}

			}
		}

		appiumServerUrl = "http://" + hostName + ":" + availablePort + "/wd/hub";

		if (!isAppiumServerRunning()) {
			throw new Exception("APPIUM SERVER IS NOT UP AND RUNNING!!!");
		}

		return appiumServerUrl;
	}

	public String getAppiumServerUrl() {
		return appiumServerUrl;
	}

	public void cleanupStaleExistingAppiumProcess() throws Exception {
		String command = this.deviceInfoObj.getDeviceID() + " | grep appium";
		if (this.deviceInfoObj.isRemoteDevice()) {
			String grepProcessPid = "ps aux | grep %s | grep -v grep | awk '{print $2}' | xargs kill -9";
			String cleanupAppiumProcesses = String.format(grepProcessPid, command);
			this.sshClientObj.executeRemoteCommand(cleanupAppiumProcesses);
		} else {
			Utils util = new Utils();
			util.killProcessesGracefully(command);
		}

	}

	public boolean isAppiumServerRunning() throws Exception {
		Response response = RestAssured.given().contentType(ContentType.JSON).get(this.getAppiumServerUrl() + "/status")
				.then().contentType(ContentType.JSON).extract().response();
		LOG.info("GET APPIUM SERVER STATUS RESPONSE: " + response.getBody().asString());
		LOG.info("GET APPIUM SERVER STATUS RESPONSE STATUS CODE: " + response.getStatusCode());
		if (response.getStatusCode() == 200) {
			return true;
		}
		return false;
	}

	private String createTemporaryFolderWithCustomPrefix(final String customDirPrefix) throws Exception {
		return COMMON_TEMP_FILE_DIR + Files.createTempDirectory(customDirPrefix).getFileName();
	}

	public File getAPPIUM_LOG_FILE() {
		return this.APPIUM_LOG_FILE;
	}

}
