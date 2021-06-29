package com.itt.android.adb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.ttl.TransmittableThreadLocal;

public class AdbFactory {
	
	private static ThreadLocal<AdbController> adbFactory = new TransmittableThreadLocal<AdbController>();
	private static Set<String> deviceList = new HashSet<String>();

	public static AdbController getAdbController() {
			return adbFactory.get();
	}
	
	public static void setAdbControllerObj(AdbController adbController) {
		deviceList.add(adbController.getDeviceID());
		adbFactory.set(adbController);
	}
	
	public static Set<String> getConnectedDeviceList() throws Exception{
		return deviceList;
	}
	public static boolean isDeviceConnected() {
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
				if(line.contains(adbFactory.get().getDeviceID())) {
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
}
