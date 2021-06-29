package com.itt.android.adb;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itt.common.Constants;
import com.itt.common.DeviceInfo;
import com.itt.common.ReadStream;
import com.itt.common.Constants.DeviceDoneCoordinates;
import com.itt.context.ITTDriverConstants;
import com.itt.context.ITTDriverContext;
import com.itt.ssh.SshClient;
import com.itt.utils.DeviceLogger;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class AdbController {

	private static final Logger LOG = LoggerFactory.getLogger(AdbController.class);
	public String adbLocation = System.getenv("ANDROID_HOME") + "/platform-tools/adb ";
	public String aaptLocation = System.getenv("ANDROID_HOME") + "/build-tools/27.0.1/aapt ";
	private BufferedReader bReader;
	private Process process;
	private String deviceID;
	private DeviceInfo deviceInfoObj;
	private static String propFileName = "config.properties";
	private SshClient sshClientObj;
	private ITTDriverContext ittDriverContext;

	
	public AdbController(String deviceID) {
		this.adbLocation = adbLocation + " -s " + deviceID + " ";
		this.deviceID = deviceID;
	}
	
	public AdbController(final DeviceInfo deviceInfoObj) {
		this.deviceInfoObj = deviceInfoObj;
		this.deviceID = this.deviceInfoObj.getDeviceID();
		this.adbLocation = adbLocation + " -s " + this.deviceID + " ";
		if (deviceInfoObj.isRemoteDevice()) {
			sshClientObj = new SshClient(deviceInfoObj.getiPAddress(), deviceInfoObj.getLoginPassword(),deviceInfoObj.getLoginUserName(),true);
			adbLocation = "$adb "+"-s " + this.deviceID + " ";;
			aaptLocation= "$aapt ";
		}
	}
	
	public AdbController(final DeviceInfo deviceInfoObj,final ITTDriverContext testDriverContext) {
		this.ittDriverContext = testDriverContext;
		this.deviceInfoObj = deviceInfoObj;
		this.deviceID = this.deviceInfoObj.getDeviceID();
		this.adbLocation = adbLocation + " -s " + this.deviceID + " ";
		if (deviceInfoObj.isRemoteDevice()) {
			sshClientObj = new SshClient(deviceInfoObj.getiPAddress(), deviceInfoObj.getLoginPassword(),deviceInfoObj.getLoginUserName(),true);
			adbLocation = "$adb "+"-s " + this.deviceID + " ";;
			aaptLocation= "$aapt ";
		}
	}

	public String getDeviceID() {
		return this.deviceID;
	}

	public boolean installPackage(String packageName) {
		
		if (deviceInfoObj.isRemoteDevice()) {
			//The Execution is Remote Device Execution and Test Resource exists in Local Repo.
        	// It has to be copied first and change the path accordingly.
        	String remoteApkFilePath =this.ittDriverContext.getAttribute(ITTDriverConstants.TEST_RESOURCES_TEMP_FILE_DIR_KEY).toString()+ITTDriverConstants.FILE_SEPARATOR+new File(packageName).getName();
        	this.sshClientObj.SCPTo(packageName, remoteApkFilePath);
        	if(!this.sshClientObj.isFileExists(remoteApkFilePath))
        	{
        		return false;
        	}
        	String command = String.format(" install -r " + remoteApkFilePath);
        	String result = executeAdbCommand(command,5*60);
        	LOG.info("Install package: " + packageName + " result is" + result);
			return result.contains("Success");
        	
		}else
		{
			String command = String.format(" install -r " + packageName);
			//In case of Battery Tests, Pushing APK can take longer time over wifi to device
			String result = executeAdbCommand(command,5*60);
			LOG.info("Install package: " + packageName + " result is" + result);
			return result.contains("Success");
		}
		
	}

	/**
	 * install package with all run time permission options.
	 * @param packageName
	 * @return
	 */
	public boolean installPackagWithGrantAllORunTimePermsOption(final String packageName) {

		if (deviceInfoObj.isRemoteDevice()) {

			//The Execution is Remote Device Execution and Test Resource exists in Local Repo.
        	// It has to be copied first and change the path accordingly.
        	String remoteApkFilePath =this.ittDriverContext.getAttribute(ITTDriverConstants.TEST_RESOURCES_TEMP_FILE_DIR_KEY).toString()+ITTDriverConstants.FILE_SEPARATOR+new File(packageName).getName();
        	this.sshClientObj.SCPTo(packageName, remoteApkFilePath);
        	if(!this.sshClientObj.isFileExists(remoteApkFilePath))
        	{
        		return false;
        	}
        	String command = String.format(" install -g -r " + remoteApkFilePath);
        	String result = executeAdbCommand(command,5*60);
        	LOG.info("Install package: " + packageName + " result is" + result);
			return result.contains("Success");
        	
		
		} else {
			final String command = String.format(" install -g -r " + packageName);
			final String result = executeAdbCommand(command);
			LOG.info("Install package: " + packageName + " result is" + result);
			return result.contains("Success");
		}

	}

    public boolean uninstallPackage(String packageName) {
        String command = String.format(" uninstall " + packageName);
        String result = executeAdbCommand(command);
        LOG.info("Uninstall package: " + packageName + " result is" + result);
        return result.contains("Success");
    }

	public boolean isPackageInstalled(String appPackage) {
    	String command = String.format(" shell pm list packages | grep " + appPackage);
		String result = executeAdbCommand(command).concat(" ");
		LOG.info("Is " + appPackage + " installed on the device?" + result);
		return result.contains("package:" + appPackage + " ");
	}
	
	public boolean isDeviceinChargingMode() {
		String acPoweredResult = executeAdbCommand(" shell dumpsys battery| grep 'AC powered'");
		String usbPoweredResult = executeAdbCommand(" shell dumpsys battery| grep 'USB powered'");
		String wireLessPowered = executeAdbCommand(" shell dumpsys battery| grep 'Wireless powered'");
		LOG.info("Is Device in Charging Mode?" + acPoweredResult +" "+usbPoweredResult);
		return acPoweredResult.contains("true")||usbPoweredResult.contains("true")||wireLessPowered.contains("true");
	}

	/**
	 * Wake up the device.
	 */
	public void wakeUpDevice() {
		LOG.info("...ADBController.wakeUpDevice()...");
		String line = null;
		try {
			if (!isDeviceScreenOn()) {

				if (this.deviceInfoObj.isRemoteDevice()) {
					this.sshClientObj.executeRemoteCommand(adbLocation + " shell input keyevent 26");

				} else {
					process = Runtime.getRuntime().exec(adbLocation + " shell input keyevent 26");
					process.waitFor(90, TimeUnit.SECONDS); // Max 90 Seconds to execute command
					if (process.isAlive()) {
						process.destroy();
					} else {
						bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
						while ((line = bReader.readLine()) != null) {
							LOG.info("wakeup=" + line);
						}
					}
				}

			} else {
				LOG.info("Device screen already on!!!");
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	/**
	 * Unlock the device with password.
	 *
	 * @param password
	 *            Password to unlock device
	 */
	public void unlockdevice(String password) {
		wakeUpDevice();
		LOG.info("...ADBController.unlockDevice()..." + adbLocation);
		try {
			executeAdbCommand(" shell input keyevent 82");
			executeAdbCommand(" shell input text " + password);
			executeAdbCommand(" shell input keyevent 66");
			executeAdbCommand(" shell input keyevent 3");
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public boolean isDeviceScreenOn() {
		LOG.info("===== isDeviceScreenOn ===== androidVersion=" + getOSVersion());
		boolean result = false;
		String line = null;
		String deciderString = (getOSVersion().equals("4.4.4")) ? "mScreenOn"
				: "mInteractive";
		
		if (this.deviceInfoObj.isRemoteDevice()) {
			List<String> logs = this.sshClientObj.executeRemoteCommand(
					adbLocation + " shell dumpsys input_method | grep "
							+ deciderString);
			result = (logs.contains("true")) ? true : false;
		}else
		{
			LOG.info("deciderString=" + deciderString);
			try {
				process = Runtime.getRuntime().exec(
						adbLocation + " shell dumpsys input_method | grep "
								+ deciderString);
				process.waitFor(90, TimeUnit.SECONDS); // Max 90 Seconds to execute command
				if (process.isAlive()) {
					process.destroy();
				} else {
					bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					while ((line = bReader.readLine()) != null) {
						LOG.info("isDeviceScreenOn=" + line);
						line = line.trim();
						line = line.substring(line.indexOf(deciderString), line.length());
						line = line.split("=")[1];
						LOG.info("result=" + line);
						result = (line.equals("true")) ? true : false;
					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			
		}
		
		return result;
	}

	/**
	 * Clear the app cache.
	 *
	 * @param appPackage
	 *            App package of the app
	 * @return result
	 */
	public boolean clearAppCache(String appPackage) {
		LOG.info("...Clear Application cache..." + appPackage);
		boolean result = false;
		String line = null;
		
		if (this.deviceInfoObj.isRemoteDevice()) {
			List<String> logs = this.sshClientObj.executeRemoteCommand(adbLocation + " shell pm  clear " + appPackage);
			LOG.info("clearAppCache=" + logs.toString());
		}else
		{
			try {
				process = Runtime.getRuntime().exec(adbLocation + " shell pm  clear " + appPackage);
				process.waitFor(90, TimeUnit.SECONDS); // Max 90 Seconds to execute command
				if (process.isAlive()) {
					process.destroy();
				} else {
					bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					while ((line = bReader.readLine()) != null) {
						LOG.info("clearAppCache=" + line);
					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		
		return result;
	}

	/**
	 * Launch app with the app package + launcher activity.
	 *
	 * @param appPackage full package name + full activity name
	 *                   e.g. com.package.name/com.somepackage.MainActivity
	 */
	public void launchApp(String appPackage) {
		LOG.info("ADBController.LaunchApp()..." + appPackage);
		executeAdbCommand(" shell am start " + appPackage);
	}

	/**
	 * Start main activity from specified package
	 * @param appPackage package to look for main activity in
	 */
	public void launchLauncherActivityFromPackage(String appPackage) {
		LOG.info("ADBController.launchLauncherActivityFromPackage() " + appPackage);
		String command = " shell monkey -p " + appPackage + " -c android.intent.category.LAUNCHER 1";
		executeAdbCommand(command);
	}

	/**
	 * Stops app and all activities with the app package.
	 *
	 * @param appPackage
	 */
	public void closeApplication(String appPackage) {
		LOG.info("ADBController.stopApp()..." + appPackage);
		String command = " shell am force-stop " + appPackage;
		executeAdbCommand(command);
	}

	/**
	 * Opens Device Admin Settings on Android phone.
	 */
	public void openDeviceAdminSettings() {
		LOG.info("ADBController.openDeviceAdminSettings()...");
		executeAdbCommand("shell am start -S com.android.settings/com.android.settings.DeviceAdminSettings");
	}

	/**
	 * Opens Security Settings on Android phone.
	 */
	public void openSecuritySettings() {
		LOG.info("ADBController.openSecuritySettings...");
		executeAdbCommand(" shell am start -a  android.settings.SECURITY_SETTINGS  --activity-clear-top");
	}

	/**
	 * Opens Network & Internet / Connections Settings on Android phone.
	 */
	public void openNetworkAndInternetSettings() {
		LOG.info("ADBController.openNetworkAndInternetSettings...");
		executeAdbCommand(" shell am start -a  android.settings.WIRELESS_SETTINGS  --activity-clear-top");
	}
	
	/**
	 * Opens Device  Settings on Android phone.
	 */
	public void openDeviceSettings() {
		LOG.info("ADBController.openDeviceSettings()...");
		executeAdbCommand("shell am start -a android.settings.SETTINGS");
	}

	/**
	 * Opens Device  Camera on Android phone.
	 */
	public void openCamera() {
		LOG.info("ADBController.openCamera()...");
		executeAdbCommand("shell am start -a android.media.action.IMAGE_CAPTURE");
	}

	/**
	 * Execute any ADB commands
	 */
	public String executeAdbCommand(String command) {

		if (this.deviceInfoObj.isRemoteDevice()) {
			StringJoiner actualtput = new StringJoiner(System.lineSeparator());
			try {
				LOG.info("EXECUTE COMMAND : " + adbLocation + command + " ON REMOTE HOST : " + sshClientObj.getHost());
				for (String tempDataLine : sshClientObj.executeRemoteCommand("/bin/sh -c '"+adbLocation + command+"'")) {
					actualtput.add(tempDataLine);
				}
				LOG.info("OUTPUT : " + actualtput.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return actualtput.toString();
		} else {
			String line = "";
			String output = "";
			LOG.info("Executing ADB Command " + adbLocation + command);
			try {
				process = Runtime.getRuntime().exec(adbLocation + command);
				process.waitFor(90, TimeUnit.SECONDS); // Max 90 Seconds to execute command

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
				e.printStackTrace();
			}
			return output;
		}

	}
	
	/**
	 * To execute commands for a given time and collect data where process keeps on running.
	 * @param timeout            
	 */
	public String executeAdbCommand(String command, int timeout) {
		if (this.deviceInfoObj.isRemoteDevice()) {
			StringJoiner actualtput = new StringJoiner(System.lineSeparator());
			try {
				LOG.info("EXECUTE COMMAND : " + adbLocation + command + " ON REMOTE HOST : " + sshClientObj.getHost());
				for (String tempDataLine : sshClientObj.executeRemoteCommand(adbLocation + command)) {
					actualtput.add(tempDataLine);
				}
				LOG.info("OUTPUT : " + actualtput.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return actualtput.toString();

		} else {
			String output = "";
			LOG.info("Executing ADB Command " + adbLocation + command);
			try {
				process = Runtime.getRuntime().exec(adbLocation + command);
				ReadStream stdErr = new ReadStream("stderr", process.getErrorStream());
				ReadStream stdOut = new ReadStream("stdin", process.getInputStream());
				stdErr.start();
				stdOut.start();
				process.waitFor(timeout, TimeUnit.SECONDS);
				if (process.isAlive()) {
					process.destroy();
				}
				return stdOut.getOutput();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return output;
		}
	}

	public String getDeviceModelNumber() {
		String modelNumber = executeAdbCommand(" shell getprop | grep ro.product.model");
		modelNumber = modelNumber.substring(21);
		modelNumber = modelNumber.replace("[", "");
		modelNumber = modelNumber.replace("]", "");
		return modelNumber;
	}
	
	/**
	 * Method to know if device supports Iris feature as one of unlocking
	 * methods
	 * 
	 * @return Boolean true - if device supports Iris feature
	 */
	public boolean isIrisSupportedOnDevice() {
		String isIrisSupported = executeAdbCommand(" shell getprop init.svc.irisd").trim();
		if (StringUtils.equalsIgnoreCase(isIrisSupported, "running")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Method to know if device supports Face Detection feature as one of
	 * unlocking methods
	 * 
	 * @return Boolean true - if device supports Face Detection feature
	 */
	public boolean isFaceDetectionSupportedOnDevice() {
		String isFaceDetectionSupported = executeAdbCommand(" shell getprop init.svc.faced").trim();
		if (StringUtils.equalsIgnoreCase(isFaceDetectionSupported, "running")) {
			return true;
		} else {
			return false;
		}
	}

    public void sendText(String text) {
        LOG.debug("ADBController.sendText : " + text);
        text = text.replace(" ", "%s");
        String command = String.format(" shell input text %s", text);
        executeAdbCommand(command);
    }

	/**
	 * Sends keyevent with adb
	 * @param keyevent keyevent code
	 */
	public void sendKeyEvent(String keyevent) {
		LOG.debug("ADBController.sendKeyEvent : " + keyevent);
		String command = String.format(" shell input keyevent %s", keyevent);
		executeAdbCommand(command);
	}

	public String getOSVersion() {
		LOG.debug("Get OS version");
		String command = String.format(" shell getprop ro.build.version.release");
		// Work around for Android 'R' Developer Preview builds automation
		if (executeAdbCommand(command).trim().equals("R")) {
			LOG.debug("This is Android 'R' Developer Preview build");
			return "11.0";
		}		
		return executeAdbCommand(command).trim();
	}

	/**
	 * @return String that looks like 1080x1920
	 */
	public String getDeviceScreenSize() {
		LOG.debug("Get device screen size");
		String command = String.format(" shell wm size");
		return executeAdbCommand(command).trim().split("Physical size: ")[1];
	}

	/**
	 * @return String that looks like 1080x1920
	 *
	 */
	public String getClickableScreenSize() {
		LOG.debug("Get device screen size");
		String command = String.format(" shell wm size");
		String response = executeAdbCommand(command).trim().split("Physical size: ")[1];
		if(response.contains("Override size")) {
			//This will return the exact clickable screen area size
			response = response.trim().split("Override size: ")[1].trim();
		}
		return response;
	}

	public String getCurrentForegroundActivityName() {
		String adbResponse = "";
		if (getAPILelvel() > 28 || getOSVersion().equals("10")) {
			LOG.info("FOUND OS '10' or > 10 OS Device.");
			adbResponse = executeAdbCommand(" shell dumpsys window displays | grep mCurrentFocus").trim();
		} else { 
			adbResponse = executeAdbCommand(" shell dumpsys window windows | grep mCurrentFocus").trim();
		}
		return adbResponse;
	}

	public int getAPILelvel() {
		return Integer.parseInt(executeAdbCommand(" shell getprop ro.build.version.sdk").trim());
	}
	
	/*
	 * Enable GPS location on device Works from Android 4.4
	 */
	public void enableLocationOnDevice() {
		executeAdbCommand(" shell settings put secure location_providers_allowed +network,+gps");
	}
	
	public boolean isLocationEnabled()
	{
		String result = executeAdbCommand(" shell settings get secure location_providers_allowed");
		return result.contains("gps")||result.contains("network");
	}
	

	/*
	 * Disable GPS Location on device - Works from Android 4.4
	 */
	public void disableLocationOnDevice() {
		executeAdbCommand(" shell settings put secure location_providers_allowed -network,-gps");
	}

	/**
	 * Enable ADB keyboard to type unicode characters (like diacritic signs or emoji)
	 * To use this make sure that ADBKeyboard app is installed on device
	 * (available in core_device_automation_framework/src/test/resources/AndroidResources/ADBKeyboard.apk)
	 */
	public void enableADBKeyboard() {
		LOG.debug("Enable ADB keyboard");
		executeAdbCommand(" shell ime set com.android.adbkeyboard/.AdbIME");
	}

	/**
	 * Disable ADB keyboard
	 */
	public void disableADBKeyboard() {
		LOG.debug("Disable ADB keyboard");
		executeAdbCommand(" shell ime set com.google.android.googlequicksearchbox/com.google.android.voicesearch.ime.VoiceInputMethodService");
		executeAdbCommand(" shell ime set com.sec.android.inputmethod/.SamsungKeypad");
		executeAdbCommand(" shell ime set com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME");
	}

	/**
	 * Send unicode symbols with ADB keyboard
	 * Example: "128568,32,67,97,116" - where numbers is unicode(decimal) codes for symbols
	 * @param symbols unicode symbols
	 */
	public void sendUnicodeSymbolsWithADBKeyboard(String symbols) {
		LOG.debug("Send unicode symbols with ADB keyboard - " + symbols);
		executeAdbCommand("  shell am broadcast -a ADB_INPUT_CHARS --eia chars '" + symbols + "'");
	}

	/**
	 * Check that device connected to network
	 */
	public boolean isConnectedToNetwork() {
		LOG.debug("Check that device connected to network");
		return !executeAdbCommand(" shell ping -c 1 8.8.8.8").contains("Network is unreachable");
	}


    /*
     * Get the App VersionName Returns the targetSDK of the app.
     */
    public String getAppVersionName(String packageId) {
        String adbResponse = executeAdbCommand(
                " shell dumpsys package " + packageId + " | grep versionName").trim();
        String strArray[] = adbResponse.split("=");
        return strArray[1];
    }

	/**
	 * @param appPackage
	 * @return targetSdk of app as integer.
	 */
	public int getTargetSdk(String appPackage) {
		String adbResponse = executeAdbCommand(" shell dumpsys package " + appPackage + " | grep targetSdk").trim();
		int indexOf = adbResponse.indexOf("targetSdk");
		return Integer.parseInt(adbResponse.substring(indexOf + 10, indexOf + 12));
	}

    /*
     * Revokes the specified permission for the app
     */
    public void revokePermission(String appPackage, String permission) {
        executeAdbCommand(" shell pm revoke " + appPackage + " "
                + permission);

    }

	/**
	 * @param adminActivity
	 * @return true if device admin is activiated
	 */
	public boolean isDeviceAdminActivated(String adminActivity) {
		String adbResponse = executeAdbCommand(" shell dumpsys device_policy").trim();
		return adbResponse.contains(adminActivity);
	}

	/**
	 *
	 * @param appPackage
	 * @param permissionName
	 * @return true if application has permission granted
	 */
	public String getPermissionState(String appPackage, String permissionName) {
		String adbResponse = executeAdbCommand(" shell dumpsys package " + appPackage + "| grep " + permissionName).trim();
		int indexOf = adbResponse.indexOf("granted");
		String grantState = adbResponse.substring(indexOf + 8, indexOf + 12);
		if (grantState.equals("true")) {
			return "true";
		} else {
			return "false";
		}
	}

	/**
	 *
	 * @param tagName - Send null if TAGNAME not available. If you pass null then it will seach for the expectedString in complete device logs.
	 * if TAG NAME provided then, expectedString will be searched in lines which are having same TAGNAME.
	 * @param expectedString - Expected string to find in adb logcat -d output
	 * <br>Usage:Pass expected String as: knoxValidation
	 * @return true if the string exists in the logs.

	 */
	public boolean searchInAdbLogs(String tagName, String expectedString) {
		
		
		String line = "";
		StringBuilder output = new StringBuilder();
		String command = "logcat -d ";
		if (tagName != null) {
			command = command + "| grep " + tagName;
		}
		if (this.deviceInfoObj.isRemoteDevice()) {
			List<String> logs = this.sshClientObj.executeRemoteCommand(adbLocation + command);
			return logs.contains(expectedString);
		}else
		{
			Process proc = null;
			try {
				LOG.info("EXECUTING COMMAND" + command);
				proc = Runtime.getRuntime().exec(adbLocation + command);
				BufferedReader bufferReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				while ((line = bufferReader.readLine()) != null) {
					output.append(line);
				}
				bufferReader.close();
				if (proc != null && proc.isAlive()) {
					proc.destroy();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (proc != null && proc.isAlive()) {
					proc.destroy();
				}
			}
			if (StringUtils.contains(output, expectedString)) {
				return true;
			}
			return false;
		}
		
		
	}

	public int stringCountAdbLogs(String tagName, String searchString) {
		String line = "";
		int counter = 0;
		StringBuilder output = new StringBuilder();
		String command = "logcat -d ";
		if (tagName != null) {
			command = command + "| grep " + tagName;
		}
		Process proc = null;

		if (this.deviceInfoObj.isRemoteDevice()) {
			List<String> logs = this.sshClientObj.executeRemoteCommand(adbLocation + command);
			for (String templine : logs) {
				if (templine.contains(tagName)) {
					for (String word : line.split(" ")) {
						if (word.trim().equals(searchString.trim())) {
							counter++;
							LOG.info("EXPECTED STRING " + searchString + " WITH TAG " + tagName
									+ " MATCH FOUND WITH OCCURENCES " + counter);
						}
					}

				}

			}

		} else {
			try {
				LOG.info("EXECUTING COMMAND" + command);
				proc = Runtime.getRuntime().exec(adbLocation + command);
				BufferedReader bufferReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				while ((line = bufferReader.readLine()) != null) {
					if (line.contains(tagName)) {
						for (String word : line.split(" ")) {
							if (word.trim().equals(searchString.trim())) {
								counter++;
								LOG.info("EXPECTED STRING " + searchString + " WITH TAG " + tagName
										+ " MATCH FOUND WITH OCCURENCES " + counter);
							}
						}
					}
				}

				bufferReader.close();
				if (proc != null && proc.isAlive()) {
					proc.destroy();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (proc != null && proc.isAlive()) {
					proc.destroy();
				}
			}

		}
		return counter;

	}
	/**
	 * Opens Lock Screen and Security Settings on Samsung phone.
	 */
	public void openLockScreenAndSecuritySettingsForSamsungDevice() {
		LOG.info("ADBController.openSecuritySettingsForSamsungDevice...");
		String line = null;
		if ((isManufacturer("samsung")) && getAPILelvel() >= 28) {
			executeAdbCommand("shell am start -a com.android.settings.LOCKSCREEN_SETTINGS");
		} else {
		try {
			process = Runtime
					.getRuntime()
					.exec(adbLocation
							+ "  shell am start -S "
							+ "\"com.android.settings/.Settings\\$LockscreenMenuActivity\"");
			process.waitFor(90, TimeUnit.SECONDS); //Max 90 Seconds to execute command

			if (process.isAlive()) {
				process.destroy();
			} else {
				bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				while ((line = bReader.readLine()) != null) {
					LOG.info(line);
				}
			}	
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	  }
	}


	public void clickOnNumericKeyboardDone() throws Exception {

		String deviceModelNumber = getDeviceModelNumber();
		switch (deviceModelNumber) {
		case "SM-T355Y":
			executeAdbCommand(Constants.DeviceDoneCoordinates.SMT355Y_NUMERICKEYBOARD_DONE);
			break;

		case "Nexus 5":
			executeAdbCommand(Constants.DeviceDoneCoordinates.NEXUS5_NUMERIC_KEYBOARD_DONE);
			break;

		case "ASUS_Z00UD":
			executeAdbCommand(Constants.DeviceDoneCoordinates.ASUS_Z00UD_NUMERICKEYBOARD_DONE);
			break;

		case "Nexus 9":
			executeAdbCommand(Constants.DeviceDoneCoordinates.NEXUS9_NUMERICKEYBOARD_DONE);
			break;

		case "Nexus 7":
			executeAdbCommand(Constants.DeviceDoneCoordinates.NEXUS7_NUMERICKEYBOARD_DONE);
			break;

		case "SM-G900H":
			executeAdbCommand(Constants.DeviceDoneCoordinates.SMG900H_NUMERICKEYBOARD_DONE);
			break;

		case "SM-G920I":
			executeAdbCommand(Constants.DeviceDoneCoordinates.SMG920I_NUMERICKEYBOARD_DONE);
			break;

		case "Nexus 5X":
			executeAdbCommand(Constants.DeviceDoneCoordinates.NEXUS5X_NUMERICKEYBOARD_DONE);
			break;

		case "SM-G925I":
			executeAdbCommand(Constants.DeviceDoneCoordinates.SMG925I_NUMERICKEYBOARD_DONE);
			break;

		case "SM-G930F":
			executeAdbCommand(Constants.DeviceDoneCoordinates.SMG930F_NUMERICKEYBOARD_DONE);
			break;

		case "SM-G935F":
			executeAdbCommand(Constants.DeviceDoneCoordinates.SMG935F_NUMERICKEYBOARD_DONE);
			break;

		case "GT-I9500":
			executeAdbCommand(Constants.DeviceDoneCoordinates.GTI9500_NUMERICKEYBOARD_DONE);
			break;

		case "E6553":
			executeAdbCommand(Constants.DeviceDoneCoordinates.E6553_NUMERICKEYBOARD_DONE);
			break;

		case "E6683":
			executeAdbCommand(Constants.DeviceDoneCoordinates.E6683_NUMERICKEYBOARD_DONE);
			break;

		case "XT1092":
			executeAdbCommand(Constants.DeviceDoneCoordinates.XT1092_NUMERICKEYBOARD_DONE);
			break;

		default:
			LOG.error("Device Model not exsits - Might need to add co-ordinates for this devic model : "
					+ deviceModelNumber);
		      throw new Exception("Device Model screen co-oridnates not exists");

		}
	}

	public void clickOnAlphaNumericKeyboardDone() throws Exception {
		final String deviceModelNumber = getDeviceModelNumber();
		LOG.info("Click on Alphanumeric Keyboard Done button and Device Model is "
				+ deviceModelNumber);

		switch (deviceModelNumber) {
		case "SM-T355Y":
			executeAdbCommand(Constants.DeviceDoneCoordinates.SMT355Y_ALPHAKEYBOARD_DONE);
			break;

		case "Nexus 5":
			executeAdbCommand(Constants.DeviceDoneCoordinates.NEXUS5_ALPHAKEYBOARD_DONE);
			break;

		case "ASUS_Z00UD":
			executeAdbCommand(Constants.DeviceDoneCoordinates.ASUS_Z00UD_ALPHAKEYBOARD_DONE);
			break;

		case "Nexus 9":
			executeAdbCommand(Constants.DeviceDoneCoordinates.NEXUS9_ALPHAKEYBOARD_DONE);
			break;

		case "Nexus 7":
			executeAdbCommand(Constants.DeviceDoneCoordinates.NEXUS7_ALPHAKEYBOARD_DONE);
			break;

		case "SM-G900H":
			executeAdbCommand(Constants.DeviceDoneCoordinates.SMG900H_ALPHAKEYBOARD_DONE);
			break;

		case "SM-G920I":
			executeAdbCommand(Constants.DeviceDoneCoordinates.SMG920I_ALPHAKEYBOARD_DONE);
			break;

		case "SM-G930F":
			executeAdbCommand(Constants.DeviceDoneCoordinates.SMG930F_ALPHAKEYBOARD_DONE);
			break;

		case "Nexus 5X":
			executeAdbCommand(Constants.DeviceDoneCoordinates.NEXUS5X_ALPHAKEYBOARD_DONE);
			break;

		case "SM-G935F":
			executeAdbCommand(Constants.DeviceDoneCoordinates.SMG935F_ALPHAKEYBOARD_DONE);
			break;

		case "SM-G925I":
			executeAdbCommand(Constants.DeviceDoneCoordinates.SMG925I_ALPHAKEYBOARD_DONE);
			break;

		case "GT-I9500":
			executeAdbCommand(Constants.DeviceDoneCoordinates.GTI9500_ALPHAKEYBOARD_DONE);
			break;

		case "E6553":
			executeAdbCommand(Constants.DeviceDoneCoordinates.E6553_ALPHAKEYBOARD_DONE);
			break;

		case "E6683":
			executeAdbCommand(Constants.DeviceDoneCoordinates.E6683_ALPHAKEYBOARD_DONE);
			break;

		case "XT1092":
			executeAdbCommand(Constants.DeviceDoneCoordinates.XT1092_ALPHAKEYBOARD_DONE);
			break;

		default:
			LOG.error("Device Model not exsits - Might need to add co-ordinates for this devic model : "
					+ deviceModelNumber);
		      throw new Exception("Device Model screen co-oridnates not exists");
		}

	}

    /**
	 * Matches the manufacturer name of the device with the user provided
	 * manufacturer name
	 */
	public boolean isManufacturer(final String manufacturerName) {
		boolean isManufacturerMatching = executeAdbCommand(" shell getprop | grep ro.product.manufacturer")
				.contains(manufacturerName);
		return isManufacturerMatching;
	}

    /**
     * Pushes file with specified path to specified destination on device
     * @param pathToFile String representation of absolute path to file
     * @param destination path on device where file will be saved
     */
    public void pushFile(String pathToFile, String destination) {
    	if (deviceInfoObj.isRemoteDevice()) {
			//The Execution is Remote Device Execution and Test Resource exists in Local Repo.
        	// It has to be copied first and change the path accordingly.
        	String remoteFile =this.ittDriverContext.getAttribute(ITTDriverConstants.TEST_RESOURCES_TEMP_FILE_DIR_KEY).toString()+ITTDriverConstants.FILE_SEPARATOR+new File(pathToFile).getName();
        	this.sshClientObj.SCPTo(pathToFile, remoteFile);
        	if(!this.sshClientObj.isFileExists(remoteFile))
        	{
        		LOG.error("File has to be transffered first since the device connected Remotely!!!");
        	}
        	LOG.info("Push file: " + pathToFile + " to " + destination);
	        executeAdbCommand(" push " + remoteFile + " " + destination);
        	
		}else
		{
			LOG.info("Push file: " + pathToFile + " to " + destination);
	        executeAdbCommand(" push " + pathToFile + " " + destination);
		}
        
    }

    /**
     * Clears ADB LOGCAT captured Logs from device
     */
    public void clearAdbLogcat() {
    	LOG.info("CLEAR ADB LOGCAT");
        executeAdbCommand(" logcat -c");
    }

	public String getSerialNumber() {
		LOG.info("Fetching the serial number of the device..");
		String serialNumber = executeAdbCommand(" shell getprop | grep ro.boot.serialno");
		serialNumber = serialNumber.substring(21);
		serialNumber = serialNumber.substring(1, serialNumber.length()-1);
		return serialNumber;
	}

	public void wakeUpDeviceWhenLocked() throws Exception {
		LOG.info("Waking up the device");
		executeAdbCommand(" shell am start -n io.appium.unlock/.Unlock");
	}

	/**
	 * Launch Accounts Screen on device
	 */
	public void openAccountSettings() {
		LOG.info("Launch Account settings page");
		executeAdbCommand("shell am start -a android.settings.SYNC_SETTINGS  --activity-clear-top");
	}

	/**
	 * Open Recent Opened Apps Menu
	 */
	public void openRecentOpenedAppsMenu() {
		LOG.info("CLICK ON RECENT OPENED APPS MENU BUTTON");
		executeAdbCommand(" shell input keyevent KEYCODE_APP_SWITCH");
	}

	/**
	 * Open user trusted credentials to view root certificates
	 */
	public void viewRootCertificates(){
		LOG.info("Opening user trusted credentials to view root certificates");
		executeAdbCommand("shell am start -a com.android.settings.TRUSTED_CREDENTIALS_USER --activity-clear-top");
	}

	/**
	 * Launch user certificates screen
	 * Applicable only for SAMSUNG Device which are >=7.0 OS versions
	 */
	public void launchUserCertificateScreen() {
		if ((Integer.parseInt((getOSVersion().split("\\.")[0])) >= 7) && isManufacturer("samsung")) {
			LOG.info("LAUNCHING NEW USER CREDENTIALS SCREEN");
			executeAdbCommand("shell am start -a com.android.settings.USER_CREDENTIALS --activity-clear-top");
		}
	}

	/**
	 * To check device is encrypted or not
	 * @return device encryption status
	 */
	public boolean isDeviceEncrypted() {
		String output = executeAdbCommand("shell getprop | grep ro.crypto.state").split(":")[1].trim().replaceAll("\\[|\\]", "");
		if(output.equalsIgnoreCase("encrypted")) {
			return true;
		}
		return false;
	}

	/**
	 * Retrieve KNOX shared device version
	 */
	public String getKnoxSharedDeviceVersion(){
		String[] knoxVersionArr = AdbFactory.getAdbController().executeAdbCommand(
				" shell getprop | grep net.knox.shareddevice.version").split(": ");
		String knoxSharedDeviceVersion = knoxVersionArr[1].substring(1,4);
		return knoxSharedDeviceVersion;
	}
	
	/**
	 * Open settings to manage all applications
	 */
	public void viewManageAllApplications(){
		executeAdbCommand(" shell am start -a android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS");
	}
	
	/**
	 * Enable Unkown Sources option under security to allow Installation of Apps.
	 */
	public void enableUnknownSources() {
		LOG.info("ENABLE UNKOWN SOURCES OPTION UNDER SECURITY TO ALLOW INSTALLATION OF APPS");
		executeAdbCommand(" shell settings put secure install_non_market_apps 1");
	}
	
	/**
	 * Enable Stay awake option
	 */
	public void enableStayAwake() {
		LOG.info("ENABLE STAY AWAKE OPTION ON DEVICE");
		executeAdbCommand(" shell settings put global stay_on_while_plugged_in 1");
	}
	
	/**
	 * Set Screen timeout to max value. Max value in Nexus 30 min and Samsung 10 min
	 */
	public void setScreenTimeoutToMaxValue() {
		LOG.info("SET SCREEN TIMEOUT VALUE");
		if (isManufacturer("LGE"))
			executeAdbCommand(" shell settings put system screen_off_timeout 2147483647");
		else
			executeAdbCommand(" shell settings put system screen_off_timeout 3600000");

	}
	
	/** Collect the device logs (Last 5000 lines) and dump into a file
	 * @param logsFilePath
	 * @throws Exception 
	 */
	public void captureDeviceLogs(String logsFilePath) throws Exception {
		
		if (this.deviceInfoObj.isRemoteDevice()) {
			DeviceLogger deviceLogger = null;
			try
			{
			LOG.info("Starting Device logging at {}", logsFilePath);
			String remoteDeviceLogFilePath =this.ittDriverContext.getAttribute(ITTDriverConstants.TEST_RESOURCES_TEMP_FILE_DIR_KEY).toString()+ITTDriverConstants.FILE_SEPARATOR+new File(logsFilePath).getName();
			String adblog = adbLocation + " logcat -t 15000 -v threadtime"+ ">> \"" + remoteDeviceLogFilePath + "\"";
			this.sshClientObj.executeRemoteCommand(adblog);
			
			}finally
			{
				if(deviceLogger!=null)
				{
					deviceLogger.stop();
				}
			}
			
			
		}else
		{
		LOG.info("DELETE EXISTING LOG FILE AND CREATE A NEW FILE");
		try {
			File file = new File(logsFilePath);
			if (file.exists() && !file.isDirectory()) {
				FileUtils.forceDelete(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String line = "";
		// We should not remove below check, otherwise it may cause jobs to hang when device is disconnected from adb
		Process proc = null; 
		BufferedReader bufferReader = null;
		BufferedWriter bufferWriter = null;
		if (isDeviceConnected()) {
			try {
				proc = Runtime.getRuntime().exec(adbLocation + " logcat -t 15000 -v threadtime");
				bufferReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				bufferWriter = new BufferedWriter(new FileWriter(logsFilePath));
				while ((line = bufferReader.readLine()) != null) {
					LOG.debug(line);
					bufferWriter.append(line);
					bufferWriter.newLine();
				}
				bufferReader.close();
				bufferWriter.close();
				if (proc != null && proc.isAlive()) {
					proc.destroy();
				}
				LOG.info("SUCESSFULLY COLLECTED DEVICE LOGS");
			} catch (Exception e) {
				LOG.error("FAILED TO COLLECT THE DEVICE LOGS");
				e.printStackTrace();
			} finally {
				if (proc != null && proc.isAlive()) {
					proc.destroy();
				}
				if (bufferReader != null) {
					bufferReader.close();
				}
				if (bufferWriter != null) {
					bufferWriter.close();
				}
			}
		} else {
			throw new Exception("DEVICE NOT CONNECTED - DEVICE SERIAL NO IS:" + AdbFactory.getAdbController().getDeviceID());
		}
		}

	}

	/**
	 * Set system time format to 12 hours or 24 hours format
	 * @param setTo12 Sets time format to 12 hours if true, otherwise to 24 houts
	 */
	public void setSystemTimeFormatTo12(boolean setTo12){
		LOG.info("Set system time fromat to " + (setTo12 ? "12" : "24"));
		executeAdbCommand("shell settings put system time_12_24 " + (setTo12 ? "12" : "24"));
	}
	
	/**
	 * Set device logcat buffer size to 4MB.
	 */
	public void setLogBufferSizeto4MB(){
		LOG.info("GET CURRENT LOG BUFFER SIZE INFO: " + executeAdbCommand(" logcat -g"));
		executeAdbCommand("logcat -G 4M");
		LOG.info("AFTER UPDATING, GET THE UPDATED LOG BUFFER SIZE INFO: " + executeAdbCommand(" logcat -g"));
	}
	
	/**
	 * Set Device Log buffer size (1MB, 2MB...4MB) - MAX IS 4.
	 * @param logBufferSize
	 */
	public void setLogBufferSizeInMB(int logBufferSize) {
		LOG.info("GET CURRENT LOG BUFFER SIZE INFO: " + executeAdbCommand(" logcat -g"));
		LOG.info("EXECUTING COMMAND ::" + "logcat -G "+ logBufferSize + "M");
		executeAdbCommand("logcat -G "+ logBufferSize + "M");
		LOG.info("AFTER UPDATING, GET THE UPDATED LOG BUFFER SIZE INFO: " + executeAdbCommand(" logcat -g"));
	}
	
	/**
	 * Opens setting to view saved wifi networks,the setting page is available only on 8.x devices. 
	 */
	public void viewSavedWifiNetworks() {
		executeAdbCommand(" shell am start -a android.settings.WIFI_SAVED_NETWORK_SETTINGS --activity-clear-top");
	}
	
	/**
	 * Checks if auto brightness settings is ON or not
	 * @return OFF if auto brightness mode is off
	 */
	public String isAutoBrightnessOn() {
		return executeAdbCommand(" shell settings get system screen_brightness_mode");
	}
	
	/**
	 * Open app Info settings screen.
	 */
	public void launchAppInfoSettings(String packageID) {
		LOG.info("LAUNCH APP INFO SCREEN OF PACKAGE ID " + packageID);
		executeAdbCommand(" shell am start -a android.settings.APPLICATION_DETAILS_SETTINGS package:" + packageID);
	}

	/**
	 * @return Toast message text on device
	 */
	public String getToastMessage() {
		String toastMessage = executeAdbCommand("logcat -d RestrictionToastManager:D *:S");
		return toastMessage;
	}

	/**
	 * Collapse notification bar
	 */
	public void collapseNotificationBar() {
		executeAdbCommand(" shell service call statusbar 2");
	}
	
	/**
	 * Launches VPN page of Android settings screens
	 */
	public void launchVPNListPage() {
		executeAdbCommand(" shell am start -a android.net.vpn.SETTINGS --activity-clear-top");
	}
	
	/**
	 * Restarting ADB Server in case Device is Offline
	 * @throws Exception
	 */
	public synchronized void restartADBServer(int maxretryCounter) throws Exception {
		
		int retryCounter = 0;
		do {
			LOG.info("ATTEMPTING ADB RESTRT SERVER***"+retryCounter+" OUT OF "+maxretryCounter);
			retryCounter++;
			if (retryCounter > maxretryCounter) {
				break;
			}
			// The restart ADB Server can be done if the current thread device is offline
			if (!AdbFactory.isDeviceConnected()) {
				LOG.info("****RESTARTING ADB SERVER***************");
				LOG.info("****DEVICE IS OFFLIENE***************");
				try {
					executeAdbCommand("kill-server");
					executeAdbCommand("start-server");
					executeAdbCommand("devices -l");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					executeAdbCommand("start-server");
				}
			} else {
				LOG.info("****DEVICE IS NOT OFFLIENE***************");
				break;
			}
		} while (!isAllDevicesOnlineNow());
	}
	
	/**
	 * Restarting ADB Server in case Device is Offline
	 * @throws Exception
	 */
	public synchronized void restartADBServer() throws Exception {
		
		int retryCounter = 0;
		do {
			retryCounter++;
			if (retryCounter > 2) {
				break;
			}
			// The restart ADB Server can be done if the current thread device is offline
			if (!AdbFactory.isDeviceConnected()) {
				LOG.info("****RESTARTING ADB SERVER***************");
				LOG.info("****DEVICE IS OFFLIENE***************");
				try {
					executeAdbCommand("kill-server");
					executeAdbCommand("start-server");
					executeAdbCommand("devices -l");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					executeAdbCommand("start-server");
				}
			} else {
				LOG.info("****DEVICE IS NOT OFFLIENE***************");
				break;
			}
		} while (!isAllDevicesOnlineNow());
	}
	
	public synchronized void reconnectDeviceOverWifi() throws Exception {
		int retryCounter = 0;
		do {
			retryCounter++;
			if (retryCounter > 2) {
				break;
			}
			for (String udid : AdbFactory.getConnectedDeviceList()) {
				if (!AdbFactory.isDeviceConnected()) {
					executeAdbCommand("connect " + udid);
				} else {
					LOG.info("****DEVICE IS NOT OFFLIENE***************");
				}
			}
		} while (!isAllDevicesOnlineNow());
	}

	/** 
	 * @return true if all the devices connected and shown online after ADB Restart
	 * @throws Exception
	 */
	public boolean isAllDevicesOnlineNow() throws Exception{
		//Getting connected devices list first
		Set<String> deviceList=AdbFactory.getConnectedDeviceList();
		for(String deviceId:deviceList) {
			if(!isGivenDeviceIdConnected(deviceId)) {
				LOG.info(deviceId+" NOT COMING BACK TO ONLINE AFTER ADB RESTART");
				return false;
			}
		}
		LOG.info("ALL DEVICES ONLINE NOW AFTER ADB RESTART");
		return true;
	}
	
	/**
	 * @param deviceId
	 * @return true if the given divice ID is online 
	 */
	public static boolean isGivenDeviceIdConnected(String deviceId) {
		String line = "null";
		boolean isDeviceConnected = false;
		String adbPath = System.getenv("ANDROID_HOME") + "/platform-tools/adb ";
		String command = adbPath +" devices";
		Runtime run = Runtime.getRuntime();
		Process pr;
		try {
			pr = run.exec(command);
			pr.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			while ((line=buf.readLine())!=null) {
				if(line.contains(deviceId)) {
					if(line.contains("unauthorized") || line.contains("offline")) {
						isDeviceConnected = false;
					} else if(line.contains("device")) {
						isDeviceConnected = true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			isDeviceConnected = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			isDeviceConnected = false;
		}
		return isDeviceConnected;
	}

	/**
	 * Execute any AAPT commands
	 */	
	public String executeAaptCommand(String command) {
		if (this.deviceInfoObj.isRemoteDevice()) {

			StringJoiner actualtput = new StringJoiner(System.lineSeparator());
			try {
				LOG.info("EXECUTE COMMAND : " + aaptLocation + command + " ON REMOTE HOST : " + sshClientObj.getHost());
				for (String tempDataLine : sshClientObj.executeRemoteCommand(adbLocation + command)) {
					actualtput.add(tempDataLine);
				}
				LOG.info("OUTPUT : " + actualtput.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return actualtput.toString();
		
		}else
		{
			String line = "";
			String output = "";
			LOG.info("Executing AAPT Command aapt " + command);
			try {
				process = Runtime.getRuntime().exec(aaptLocation + command);
				process.waitFor(90, TimeUnit.SECONDS); //Max 90 Seconds to execute command

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
				e.printStackTrace();
			}
			return output;
		}
		
	}	
		
    /*
     * Get the App VersionCode and Returns it.
     */
    public String getAppVersionCodeByAapt(String apklocation) {
        String adbResponse[] = executeAaptCommand(" dump badging " + apklocation + " | grep versionName").trim().split(" ");
        String versionArray[] = adbResponse[2].split("=");
        String versioncode = versionArray[1].replace("'","");
        LOG.info("VERSION CODE VALUE = " + versioncode);
        return versioncode;
    }	
    
    /**
     * Opens Available virtual keyboard screen
     */
    public void openAvailableVirtualKeyboardScreen() {
		executeAdbCommand(" shell am start -a android.settings.INPUT_METHOD_SETTINGS");
    }
    
    /**
     * Gets lockdown policy parameters for work profile from dumpsys
     */
	public String getDevicePolicyOutput() {
		String lockdownDump = executeAdbCommand(" shell dumpsys device_policy | grep 'provisioningState: 3' -A 65");
		return lockdownDump;
	}
	
	/**
     * Open SELECT WIFI SCREEN
     */
	public void launchWifiScreen() {
		LOG.info("OPEN WIFI SCREEN");
		if (AdbFactory.getAdbController().getAPILelvel() >= 28) {
			executeAdbCommand("shell am start -a android.settings.WIFI_SETTINGS --activity-clear-top");
		} else {
			executeAdbCommand("shell am start -a android.net.wifi.PICK_WIFI_NETWORK --activity-clear-top");
		}
	}
   
    /**
     * This method can return the Package Name for given APK
     * @param apklocation
     * @return
     */
    public String getAppPackageNameByAapt(String apklocation) {
        String adbResponse[] = executeAaptCommand(" dump badging " + apklocation + " | grep package:").trim().split(" ");
        String packageArray[] = adbResponse[1].split("=");
        String packageName = packageArray[1].replace("'","");
        return packageName;
    }
     
    /**
     * This method can return the Package Version Name for given APK
     * @param apklocation
     * @return
     */
    public String getAppVersionNameByAapt(String apklocation) {
        String adbResponse[] = executeAaptCommand(" dump badging " + apklocation + " | grep versionName:").trim().split(" ");
        String versionArray[] = adbResponse[3].split("=");
        String versionName = versionArray[1].replace("'","");
        return versionName;
    }
    
    public String[] getLogWithTagName(String tagName) {
		String line = "";
		StringBuilder output = new StringBuilder();
		String[] str;
		String command = "logcat -d ";
		if (tagName != null) {
			command = command + tagName + ":S";
		}
		Process proc = null;
		try {
			LOG.info("EXECUTING COMMAND" + command);
			proc = Runtime.getRuntime().exec(adbLocation + command);
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = bufferReader.readLine()) != null) {
				if (line.contains(tagName)) {
					output.append(line);
					output.append(",");
				}
			}
			bufferReader.close();
			if (proc != null && proc.isAlive()) {
				proc.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (proc != null && proc.isAlive()) {
				proc.destroy();
			}
		}
		
		str= output.toString().split(",");
		return str;
	}
    
    /**
     * Execute any command
     */
	public String executeCommand(String[] command) {
		Process process;
		String line = "";
		String output = "";
		StringBuilder sb = new StringBuilder();
		for (String partCommand : command) {
			sb.append(partCommand).append(" ");
		}

		if (this.deviceInfoObj.isRemoteDevice()) {
			StringJoiner actualtput = new StringJoiner(System.lineSeparator());
			String commandToExecureRemotely = "/bin/sh -c" + command[command.length - 1];
			try {
				LOG.info("EXECUTE COMMAND : " + commandToExecureRemotely + " ON REMOTE HOST : " + sshClientObj.getHost());
				for(String tempDataLine :sshClientObj.executeRemoteCommand(commandToExecureRemotely))
				{
					actualtput.add(tempDataLine);
				}
				LOG.info("OUTPUT : " + actualtput.toString());
				return actualtput.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			LOG.info("Executing Command " + sb.toString());

			try {
				process = Runtime.getRuntime().exec(command);
				process.waitFor(90, TimeUnit.SECONDS); // Max 90 Seconds to execute command

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
				e.printStackTrace();
			}
			LOG.info("output:" + output);
		}
		return output;
	}
	
	public boolean isDeviceConnected() {
		LOG.info("DEVICE CONNECTED -  DEVICE ID:  " + deviceID);
		String command = " get-state";
		String response = executeAdbCommand(command).toLowerCase().trim();
		if (response.equalsIgnoreCase("device")) {
			return true;
		} else {
			LOG.info("ADB GET-STATE RESPONSE: " + response);
			return false;
		}
		/*String line = "null";
		boolean isDeviceConnected = false;
		String adbPath = System.getenv("ANDROID_HOME") + "/platform-tools/adb ";
		String command = adbPath +" devices";
		Runtime run = Runtime.getRuntime();
		Process pr;
		try {
			pr = run.exec(command);
			pr.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			while ((line=buf.readLine())!=null) {
				if(line.contains(deviceID)) {
					if(line.contains("unauthorized") || line.contains("offline")) {
						isDeviceConnected = false;
					} else if(line.contains("device")) {
						isDeviceConnected = true;
						return true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			isDeviceConnected = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			isDeviceConnected = false;
		}
		return isDeviceConnected;*/
	}
	
	/**
	 * Fetch the screenshot from Android device and save to local system.
	 * @param localSystemFilePath localSystemFilePath.png
	 */
    public void getScreenShotFromDevice(String localSystemFilePath) {
		String SHELL = "/bin/sh";
		String SHELL_OPTS = "-c";
		String fetchScreenshotCommand = adbLocation + "exec-out screencap -p > \"" + localSystemFilePath +"\"";
		String[] command = { SHELL, SHELL_OPTS, fetchScreenshotCommand };
		executeCommand(command);
    }

    /**
     * Takes photo in IMAGE_CAPTURE activity
     */
    public void takePhoto() throws InterruptedException {
        executeAdbCommand("shell input keyevent 27");
        Thread.sleep(5000); // need to render captured photo


    }

    /**
     * Takes photo in IMAGE_CAPTURE activity
     */
    public void launchCamera() {
        executeAdbCommand("am start -a android.media.action.IMAGE_CAPTURE");
    }

	/**
	 * Trun on Bluetooth - But User has to accept the allow button
	 * @throws Exception
	 */
	public void turnOnBluetooth() throws Exception {
		String enableBluetoothCommand = " shell am start -a android.bluetooth.adapter.action.REQUEST_ENABLE";
		executeAdbCommand(enableBluetoothCommand);
	}
	
	/**
	 * Trun Off Bluetooth - But User has to accept the allow button
	 * @throws Exception
	 */
	public void turnOffBluetooth() throws Exception {
		String disableBluetoothCommand = " shell am start -a android.bluetooth.adapter.action.REQUEST_DISABLE";
		executeAdbCommand(disableBluetoothCommand);
	}

	/**
	 * Returns true if bluetooth enabled, else returns false
	 * @return
	 * @throws Exception
	 */
	public boolean isBluettoothEnabled() throws Exception {
		String searchCommand = " shell dumpsys bluetooth_manager | grep 'Bluetooth Status' -A 10";
		String statusOfbluetooth = executeAdbCommand(searchCommand);
		LOG.info(" BLUETTOOTH STATUS REPONSE" + statusOfbluetooth.toLowerCase().trim());
		if (statusOfbluetooth.toLowerCase().trim().contains("enabled: true")) {
			return true;
		}
		LOG.info("RESPONE: " + statusOfbluetooth);
		return false;
	}
	
	/**
	 * Check the WIFI state on the device.
	 * 
	 * @return true if the wifi is enabled on the device else return false.
	 */
	public boolean isWIFIEnabled() {
		String wifiStatisCommand = " shell dumpsys wifi | grep 'Wi-Fi is enabled'";
		return executeAdbCommand(wifiStatisCommand).contains("Wi-Fi is enabled");
	}
	
	/**
	 * Check the WIFI state on the device.
	 * 
	 * @return true if the wifi disabled on the device else return true.
	 */
	public boolean isWIFIDisabled() {
		String wifiStatisCommand = " shell dumpsys wifi | grep 'Wi-Fi is disabled'";
		return executeAdbCommand(wifiStatisCommand).contains("Wi-Fi is disabled");
	}
	
	/**
	 * Trun on WIFI 
	 * @throws Exception
	 */
	public void turnOnWIFI() throws Exception {
		String enableWIFICommand = " shell am broadcast -a io.appium.settings.wifi --es setstatus enable";
		executeAdbCommand(enableWIFICommand);
	}
	
	/**
	 * Trun off WIFI
	 * @throws Exception
	 */
	public void turnOffWIFI() throws Exception {
		String disableWIFICommand = " shell am broadcast -a io.appium.settings.wifi --es setstatus disable";
		executeAdbCommand(disableWIFICommand);
	}
	
	/**
	 * Reboot Device
	 * @throws Exception
	 */
	public void rebootDevice() throws Exception {
		String deviceRebootCommand = " reboot";
		executeAdbCommand(deviceRebootCommand);
	}
	
	/**
	 * Gets the Security Patch Level of the device
	 * @return Security Patch Level in String
	 * @throws Exception
	 */
	public String getMinSecurityPatchLevelFromDevice() throws Exception {
		String minPatchLevelCommand = " shell getprop ro.build.version.security_patch";
		return executeAdbCommand(minPatchLevelCommand);
	}

	/*
	 * Install test enabled APK on to the device.
	 * @param apkFilePath
	 * @return
	 */
	public boolean installTestEnabledAPK(String apkFilePath) {
		if (deviceInfoObj.isRemoteDevice()) {
			//The Execution is Remote Device Execution and Test Resource exists in Local Repo.
        	// It has to be copied first and change the path accordingly.
        	String remoteApkFilePath =this.ittDriverContext.getAttribute(ITTDriverConstants.TEST_RESOURCES_TEMP_FILE_DIR_KEY).toString()+ITTDriverConstants.FILE_SEPARATOR+new File(apkFilePath).getName();
        	this.sshClientObj.SCPTo(apkFilePath, remoteApkFilePath);
        	if(!this.sshClientObj.isFileExists(remoteApkFilePath))
        	{
        		return false;
        	}
        	String command = String.format(" install -t -r " + remoteApkFilePath);
        	String result = executeAdbCommand(command,5*60);
        	LOG.info("Install package: " + apkFilePath + " result is" + result);
			return result.contains("Success");
        	
		}else
		{
			String command = String.format(" install -t -r " + apkFilePath);
			String result = executeAdbCommand(command);
			LOG.info("Install package: " + apkFilePath + " result is" + result);
			return result.contains("Success");
		}
		
	}
	
	/**
	 * Access default Apps screen on the device 
	 */
	public void launchDefaultAppsScreen() {
		LOG.info("ACCESS DEFAULT APPS ON THE DEVICE");
		executeAdbCommand(" shell am start -a android.settings.MANAGE_DEFAULT_APPS_SETTINGS");
	}
	
	/**
	 * Removes the specified file from device
	 * @param filePath
	 */
	public void removeFileFromDevice(String filePath) {
		String command = String.format(" shell rm -f "+ filePath);
		LOG.info("COMMAND TO REMOVE FILE FROM DEVICE "+command);
		executeAdbCommand(command);
	}
	
	/**
	 * Gets IMEI number of the device
	 */
	public String getIMEIFromDevice() {
		String command = " shell service call iphonesubinfo 1 | grep -o \"[0-9a-f]\\{8\\} \" | tail -n+3 | while read a; do echo -n \"\\u${a:4:4}\\u${a:0:4}\"; done";
		LOG.info("COMMAND TO GET IMEI FILE FROM DEVICE: "+command);
		return executeAdbCommand(command);
	}
	
	public void grantPermission(String appPackage, String permission) {
        executeAdbCommand(" shell pm grant " + appPackage + " "
                + permission);
    }
	
	/**
	 * Gets IP Address from the device
	 */
	public String getIPAddressFromDevice() {
		String command = " shell ip route";
		LOG.info("COMMAND TO GET IP ADDRESS FROM THE DEVICE: "+command);
		return executeAdbCommand(command);
	}

}
