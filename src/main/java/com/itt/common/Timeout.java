package com.itt.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timeout {
	private static final Logger LOG = LoggerFactory.getLogger(Timeout.class);

	public static int ONE_SEC = 1;
	public static int DEFAULT_TIMEOUT = 30 * ONE_SEC;
	public static int FIND_ELEMENT_TIMEOUT = 2 * ONE_SEC;
	public static int ALERT_TIMEOUT = 3 * ONE_SEC;
	public static int TWO_SECONDS_TIMEOUT = 2 * ONE_SEC;
	public static int TWENTY_SECONDS_TIMEOUT = 20 * ONE_SEC;
	public static int THREE_SECONDS_TIMEOUT = 3 * ONE_SEC;
	public static int FIVE_SECONDS_TIMEOUT = 5 * ONE_SEC;
	public static int TEN_SECONDS_TIMEOUT = 10 * ONE_SEC;
	public static int FIFTEEN_SECONDS_TIMEOUT = 15 * ONE_SEC;
	public static int THIRTY_SECONDS_TIMEOUT = 30 * ONE_SEC;
	public static int SIXTY_SECONDS_TIMEOUT = 60 * ONE_SEC;
	public static int IMPLICT_SECONDS_TIMEOUT = 30 * ONE_SEC;
	public static int SCRIPT_TIMEOUT = 300 * ONE_SEC;
}
