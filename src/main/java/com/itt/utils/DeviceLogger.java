package com.itt.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itt.common.ReadStream;

public class DeviceLogger implements Runnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(DeviceLogger.class);
    
    private volatile boolean keepAlive;
    private Process process;
    private String command;
    
    private final String SHELL = "/bin/sh";
    private final String SHELL_OPTS = "-c";
    
    public DeviceLogger(String logCommand) {
        this.command = logCommand;
     }
    
    public void setKeepAlive(Boolean state) {
    		keepAlive = state;
    }
    
	public synchronized void stop() {
		process.destroy();
		LOG.info("STOPPING THE LOG FOR TEST CASE");
	}

    @Override
	public void run() {
		LOG.info("START LOGGING FOR TEST CASE");
		while (true) {
			try {
				runProcessTillAlive();
				if (!keepAlive) {
					stop();
					break;
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    
	private void runProcessTillAlive() throws IOException, InterruptedException {
		String line = "";
		String output = "";
		String[] actualCommand = { SHELL, SHELL_OPTS, command };
		Runtime r = Runtime.getRuntime();
		process = r.exec(actualCommand);
		ReadStream stdErr = new ReadStream("stderr", process.getErrorStream());
		ReadStream stdOut = new ReadStream("stdin", process.getInputStream());
		stdErr.start();
		stdOut.start();
		InputStream error = process.getErrorStream();
		InputStreamReader isrerror = new InputStreamReader(error);
		BufferedReader bre = new BufferedReader(isrerror);
		try {
			while ((line = bre.readLine()) != null) {
				LOG.info(line);
			}
			LOG.info("ERROR OCCURED WHILE START LOGGING!!!");
			this.keepAlive = false;
		} catch (Exception e) {
			LOG.info("No Error Stream!!!"+e.getMessage());
		} finally {
			bre.close();
		}
		
		process.waitFor();
		
		
	}

}
