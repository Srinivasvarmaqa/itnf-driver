package com.itt.appium;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.itt.android.adb.AdbFactory;

public class AppiumServerFactory {

	private static final Logger LOG = LoggerFactory.getLogger(AppiumServerFactory.class);

	private static ThreadLocal<InvokeAppiumServer> appiumServerFactory = new TransmittableThreadLocal<InvokeAppiumServer>();

	private static Process process;

	public static InvokeAppiumServer getInvokeAppiumServer() {
		return appiumServerFactory.get();
	}

	public static void setInvokeAppiumServer(InvokeAppiumServer appiumServer) {
		appiumServerFactory.set(appiumServer);
	}

	public static boolean isAppiumServerRunning() {
		String URL = getInvokeAppiumServer().getAppiumServerUrl();
		return !URL.isEmpty();
	}

	public static String getAppiumServerPort() {
		String URL = getInvokeAppiumServer().getAppiumServerUrl();
		return URL.split(":")[2].split("/")[0];
	}

	public static void killAppiumServer() {

		String port = getAppiumServerPort();
		String appiumServerExistingSessionKillCommand = String.format(
				"ps -ef| grep node | grep appium | grep '%s' | awk {'print $2'} | xargs kill -9",
				AdbFactory.getAdbController().getDeviceID());
		String uiAutomatorKillCommand = "ps -ef|grep uiautomator|grep " + AdbFactory.getAdbController().getDeviceID()
				+ "|grep -v grep|awk {'print $2'}| xargs kill -9";
		String getuiAutomatorExistingPortCommand = "ps -ef|grep uiautomator|grep "
				+ AdbFactory.getAdbController().getDeviceID() + "|grep -v grep|awk {'print $2'}";
		String getAppiumServerExistingPortCommand = String.format(
				"ps -ef| grep node | grep appium | grep '%s' | awk {'print $2'}",
				AdbFactory.getAdbController().getDeviceID());

		LOG.info("GET THE EXISTING UIAUTOMATOR PORT BEFORE KILLING " + getuiAutomatorExistingPortCommand);
		executeShellCommand(getuiAutomatorExistingPortCommand);

		LOG.info("GET THE EXISTING APPIUM SERVER PORT BEFORE KILLING " + getAppiumServerExistingPortCommand);
		executeShellCommand(getAppiumServerExistingPortCommand);

		LOG.info("KILLING EXISTING UIAUTOMATOR WITH THE COMMAND " + uiAutomatorKillCommand);
		executeShellCommand(uiAutomatorKillCommand);

		LOG.info("KILLING EXISTING APPIUM SERVER WITH THE COMMAND " + appiumServerExistingSessionKillCommand);
		executeShellCommand(appiumServerExistingSessionKillCommand);

		LOG.info("GET THE EXISTING UIAUTOMATOR PORT BEFORE KILLING " + getuiAutomatorExistingPortCommand);
		executeShellCommand(getuiAutomatorExistingPortCommand);

		LOG.info("GET THE EXISTING APPIUM SERVER PORT BEFORE KILLING " + getAppiumServerExistingPortCommand);
		executeShellCommand(getAppiumServerExistingPortCommand);

		LOG.info("KILLING EXISTING UIAUTOMATOR WITH THE COMMAND " + uiAutomatorKillCommand);
		executeShellCommand(uiAutomatorKillCommand);

		LOG.info("KILLING EXISTING APPIUM SERVER WITH THE COMMAND " + appiumServerExistingSessionKillCommand);
		executeShellCommand(appiumServerExistingSessionKillCommand);

	}

	public static void executeShellCommand(String command) {
		String line = "";
		String output = "";
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command("sh", "-c", command);
			builder.directory(new File(System.getProperty("user.home")));
			process = builder.start();
			process.waitFor(90, TimeUnit.SECONDS);
			if (process.isAlive()) {
				process.destroy();
			} else {
				BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
				while ((line = buffer.readLine()) != null) {
					LOG.info(line);
					output = output + " " + line;
				}
				buffer.close();
			}

		} catch (Exception e) {
			output = output + " " + e.getMessage();
			e.printStackTrace();
		} finally {
			LOG.info("SHELL COMMAND OUTPUT/RESPONSE " + output);
		}
	}

}
