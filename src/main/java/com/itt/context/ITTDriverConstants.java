package com.itt.context;

public class ITTDriverConstants {
	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	//Test Driver Context Map Key Constants
	public static final String DEVICE_HUB_DEVICE_LOG_PATH_KEY="DEVICE_HUB_DEVICE_LOG_PATH";
	public static final String DEVICE_HUB_APPIUM_LOG_PATH_KEY="DEVICE_HUB_APPIUM_LOG_PATH";
	public static final String TEST_CLIENT_DEVICE_LOG_PATH_KEY="TEST_CLIENT_DEVICE_LOG_PATH";
	public static final String TEST_EXECUTION_ID_KEY="TESTRUN_EXECUTION_ID";
	public static final String DEVICE_TEMP_FILE_DIR_KEY = "DEVICE_TEMP_FILE_DIR";
	public static final String TEST_RESOURCES_TEMP_FILE_DIR_KEY = "TEST_RESOURCES_TEMP_FILE_DIR";
	public static final String TEST_DEVICELOGS_TEMPFOLDER_KEY = "TEST_DEVICELOGS_TEMPFOLDER";
	public static final String IS_COPYING_ARTIFACTS_FROM_DEVICEHUB_REQUIRED_KEY = "IS_COPYING_ARTIFACTS_FROM_DEVICEHUB_REQUIRED";
	public static final String WDA_SERVER_PORT_KEY="WDA_SERVER_PORT_KEY";
	
	//Remote Device Hub Typical Artifacts folder names
	public static final String TEST_RESOUCES_FOLDER_NAME = "testresources";
	public static final String DEVICE_LOGS_FOLDER_NAME = "devicelogs";
	public static final String APPIUM_LOGS_FOLDER_NAME = "appiumlogs";
	public static final String IPROXY_LOGS_FOLDER_NAME = "iproxy";
	public static final String XCODEBUILD_LOGS_FOLDER_NAME = "xcodebuild";
	public static final String BASH_SCRIPT_FOLDER_NAME = "bash";
	public static final String CUSTOM_KEYCHAIN_FOLDER_NAME = "keychaindb";

	//String Format Templates
	public static final String DEVICE_TEMP_FILE_DIR_PATH = "/Volumes/ec2-user/efs/%s/%s";
	public static final String XCODEBUILD_LOG_FILENAME = "wdabuild_%d.log";
	public static final String IPROXY_LOG_FILENAME = "iproxy_%d.log";

}
