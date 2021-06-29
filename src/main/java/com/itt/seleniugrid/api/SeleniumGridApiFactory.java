package com.itt.seleniugrid.api;

import org.slf4j.LoggerFactory;

import com.itt.common.BrowserInfo;
import com.itt.context.ITTDriverContext;

import org.slf4j.Logger;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class SeleniumGridApiFactory {

	private static final Logger LOG = LoggerFactory
			.getLogger(SeleniumGridApiFactory.class);
	private String hostName = "selenium-hub";
	private String availablePort = "4444";
	private String hubUrl;
	private BrowserInfo browserInfoObj;
	private ITTDriverContext ittDriverContext;

	public SeleniumGridApiFactory(BrowserInfo browserInfo) throws Exception {
		LOG.info("Selenium Gride");
		this.browserInfoObj = browserInfo;
	}

//	public boolean isSeleniumHubRunning() throws Exception {
//		hubUrl = "http://" + hostName + ":" + availablePort + "/wd/hub";
//		Response response = RestAssured.given().contentType(ContentType.JSON)
//				.get(hubUrl + "/status").then()
//				.contentType(ContentType.JSON).extract().response();
//		LOG.info("GET SELENIUM HUB RESPONSE: " + response.getBody().asString());
//		LOG.info("GET SELENIUM HUB RESPONSE STATUS CODE: "
//				+ response.getStatusCode());
//		if (response.getStatusCode() == 200) {
//			return true;
//		}
//		return false;
//	}

	public boolean isSeleniumHubRunning() throws Exception {
		hubUrl = this.browserInfoObj.getSeleniumHubURL() + "/wd/hub";
		Response response = RestAssured.given().contentType(ContentType.JSON).get(hubUrl + "/status").then()
				.contentType(ContentType.JSON).extract().response();
		LOG.debug("GET SELENIUM HUB RESPONSE: " + response.getBody().asString());
		LOG.debug("GET SELENIUM HUB RESPONSE STATUS CODE: " + response.getStatusCode());
		if (response.getStatusCode() == 200) {
			return true;
		}
		return false;
	}
}
