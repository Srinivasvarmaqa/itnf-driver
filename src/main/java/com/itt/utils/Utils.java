package com.itt.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itt.common.ReadStream;
import com.itt.ssh.SshClientFactory;

public class Utils {
    
    private final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private Process process;
    
    private final String SHELL = "/bin/sh";
    private final String SHELL_OPTS = "-c";
    
    
    /**
     * Execute any commands
     */
    public String executeCommand(String[] command) {
        StringBuilder sb = new StringBuilder();
        for (String partCommand : command) {
            sb.append(partCommand).append(" ");
        }
        LOG.info("Executing Command " + sb.toString());
        try {
        		String line = "";
            process = Runtime.getRuntime().exec(command);
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
			} catch (Exception e) {
				LOG.info("No Error Stream!!!"+e.getMessage());
			} finally {
				bre.close();
			}
			
            process.waitFor(90, TimeUnit.SECONDS); //Max 90 Seconds to execute command
            if (process.isAlive()) {
                process.destroy();
            } 
            return stdOut.getOutput();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * Execute any commands
     */
    public String executeCommand(String[] command, int cmdWaitTimeOut) {
        StringBuilder sb = new StringBuilder();
        for (String partCommand : command) {
            sb.append(partCommand).append(" ");
        }
        LOG.info("Executing Command " + sb.toString());
        try {
        		String line = "";
            process = Runtime.getRuntime().exec(command);
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
			} catch (Exception e) {
				LOG.info("No Error Stream!!!"+e.getMessage());
			} finally {
				bre.close();
			}
			
            process.waitFor(cmdWaitTimeOut, TimeUnit.SECONDS);
            if (process.isAlive()) {
                process.destroy();
            } 
            return stdOut.getOutput();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public void executeCommandWithoutDestroy(String[] command) {
        try {
            process = Runtime.getRuntime().exec(command);
            ReadStream stdErr = new ReadStream("stderr", process.getErrorStream());
            ReadStream stdOut = new ReadStream("stdin", process.getInputStream());
            stdErr.start();
            stdOut.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void killProcessesGracefully(String...processNames ) {
        String grepProcessPid = "ps aux | grep '%s' | grep -v grep | awk '{print $2}' | xargs kill -9";
        for (String processName : processNames) {
            LOG.info("Killing the preocess {}", processName); 
            String [] killCommand = {SHELL, SHELL_OPTS, String.format(grepProcessPid, processName)};
            executeCommand(killCommand);
        }
    }
    
    public boolean isProcessRunning(String processName) {
        String grepProcessPid = "ps aux | grep '%s' | grep -v grep |awk '{print $2}'";
        String [] listProcesses = {SHELL, SHELL_OPTS, String.format(grepProcessPid, processName)};
        String output = executeCommand(listProcesses);
        return (output.split("\\s+").length > 0);
    }
    
    public void openFileOnMac(String filePath) {
    	  String openCommandWithFile = "open %s";
    	  String [] macOpenCommandWithFile = {SHELL, SHELL_OPTS, String.format(openCommandWithFile, filePath)};
    	  SshClientFactory.getSshClient().executeRemoteCommand(String.format(openCommandWithFile, filePath));
    	  //executeCommand(macOpenCommandWithFile);
    }
    
    public boolean isPortInUse(String hostName, int portNumber) {
		boolean result = true;
		Socket s = null;
		String reason = null;
		try {
			s = new Socket(hostName, portNumber);
			s.setReuseAddress(true);

			SocketAddress sa = new InetSocketAddress(hostName, portNumber);
			s.connect(sa, 2 * 60 * 1000);

			LOG.info("FREE PORT FOUND TO START THE APPIUM SERVER: " + portNumber);
			result = true;
		} catch (IOException e) {
			if (e.getMessage().contains("Connection refused")) {
				reason = "port " + portNumber + " on " + hostName + " is free.";
				result = false;
			}
			;
			if (e instanceof UnknownHostException) {
				reason = "device Hub " + hostName + " is unresolved.";
			}
			if (e instanceof SocketTimeoutException) {
				reason = "timeout while attempting to reach device hub " + hostName + " on port " + portNumber;
			}
		} catch (Exception e) {
			result = false;
		} finally {
			if(reason!=null)
			{
				LOG.info(reason);
			}
			if(s!=null) {
		        try {
		            s.close();
		        }catch (Exception e) {
		            LOG.info("ERROR WHILE CLOSING THE SOCKET "+ e.getMessage());   
		        }
		    }
		}
		return (result);
	}

    
    public boolean isWindows() {

		return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);

	}
    
    public static boolean isMac() {
    	return (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);
    }
    
    public static boolean isUnix() {
    	String OS = System.getProperty("os.name").toLowerCase();
    	return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    }

}
