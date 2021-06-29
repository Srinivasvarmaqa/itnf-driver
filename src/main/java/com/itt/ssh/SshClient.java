package com.itt.ssh;


import java.io.File;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.itt.common.Utils;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;



public class SshClient {

	private static final Logger LOG = LoggerFactory.getLogger(SshClient.class);
	private SSHClientPassword sshClientPassword;
	private String host;
	private String password;
	private String user;
	private String homeDirPath;
	private Properties config = new Properties();
	private boolean withSSHPass = false;

	/**
	 * Constructor with a check for aws instances and VMs.
	 * 
	 * @param remoteHost
	 * @param remotePassword
	 * @param user
	 */
	public SshClient(final String remoteHost, final String remotePassword, final String user) {

		this.user = user;
		this.host = remoteHost;
		this.password = remotePassword;
		this.sshClientPassword = new SSHClientPassword(remoteHost, remotePassword, user);
		this.config.put("StrictHostKeyChecking", "no");
	}

	/**
	 * Constructor with a check for aws instances and VMs.
	 * 
	 * @param remoteHost
	 * @param remotePassword
	 * @param user
	 */
	public SshClient(final String remoteHost, final String remotePassword, final String user,
			final boolean withSSHPass) {
		this.user = user;
		this.host = remoteHost;
		this.password = remotePassword;
		this.sshClientPassword = new SSHClientPassword(remoteHost, remotePassword, user);
		this.config.put("StrictHostKeyChecking", "no");
		this.withSSHPass = withSSHPass;
	}

	public SshClient() {
	}

	/**
	 * Executes the specified command.
	 *
	 * @param command
	 * @return list of messages from remote server.
	 */
	public List<String> executeRemoteCommand(final String command) {
		return this.sshClientPassword.executeRemoteCommand(command);

	}

	/**
	 * Executes the specified command.
	 *
	 * @param command
	 * @return list of messages from remote server.
	 */
	public void launchService(final String command) {
		this.sshClientPassword.launchNodeJSService(command);
	}

	/**
	 * SCP
	 * 
	 * @param srcFilePath
	 * @param destFilePath
	 * @return
	 */
	public String SCPTo(final String srcFilePath, final String destFilePath) {
		String response, scpCommand = null;
		int maxTimeOut = 10 * 60 * 60;
		if (this.withSSHPass) {
			scpCommand = String.format(
					"sshpass -p \"%s\" rsync -rvz -e 'ssh -o StrictHostKeyChecking=no -p 22' scp '%s' '%s'@%s:%s",
					this.sshClientPassword.getPassword(), srcFilePath, SshClientFactory.getSshClient().getUser(),
					SshClientFactory.getSshClient().getHost(), destFilePath);

		} else {
			scpCommand = String.format("scp %s '%s'@%s:%s", srcFilePath, SshClientFactory.getSshClient().getUser(),
					SshClientFactory.getSshClient().getHost(), destFilePath);
		}
		LOG.info("SCP COMMAND TO COPY FILE FROM LOCAL TO REMOTE SERVER " + scpCommand);
		String[] cmdd = new String[] { "bash", "-c", scpCommand };
		response = new Utils().executeCommand(cmdd, maxTimeOut);
		LOG.info("SCP COMMAND RESPONSE " + response);
		return response;
	}

	/**
	 * Create JSch Session
	 *
	 * @param i port number.
	 * @return {@link Session}
	 * @throws JSchException - pass through.
	 */
	private Session createSessionWithSpecificPort(final int i) throws JSchException {
		Session session = null;
		final JSch jsch = new JSch();
		session = jsch.getSession(this.user, this.host, i);
		session.setPassword(this.password);
		session.setConfig(this.config);
		session.connect();
		return session;
	}

	public String SCPToBySSHPass(final String srcFilePath, final String destFilePath) {
		String scpCommand = String.format("scp %s '%s'@%s:%s", srcFilePath, SshClientFactory.getSshClient().getUser(),
				SshClientFactory.getSshClient().getHost(), destFilePath);
		LOG.info("SCP COMMAND TO COPY FILE FROM LOCAL TO REMOTE SERVER " + scpCommand);
		String[] cmdd = new String[] { "bash", "-c", scpCommand };
		String response = new Utils().executeCommand(cmdd);
		LOG.info("SCP COMMAND RESPONSE " + response);
		return response;
	}

	public void SecureCopyFrom(final String remoteFilePath, final String localFilePath) {
		this.sshClientPassword.copyFileFrom(remoteFilePath, localFilePath);
	}

	public String SCPFrom(final String remoteFilePath, final String localFilePath) {
		String response = "";
		if (!this.withSSHPass) {
			String scpCommand = String.format("scp -r '%s'@%s:'%s' '%s'", SshClientFactory.getSshClient().getUser(),
					SshClientFactory.getSshClient().getHost(), remoteFilePath, localFilePath);
			LOG.info("SCP COMMAND TO COPY FILE FROM REMOTE TO LOCAL SERVER " + scpCommand);
			String[] cmdd = new String[] { "bash", "-c", scpCommand };
			response = new Utils().executeCommand(cmdd);
			LOG.info("SCP COMMAND RESPONSE " + response);
			return response;
		} else {
			this.sshClientPassword.copyFileFrom(remoteFilePath, localFilePath);
		}

		return response;

	}

	public String getFileContent(final String remoteFilePath) throws Exception {
		return this.sshClientPassword.getFileContent(remoteFilePath);
	}

	public boolean isFileExists(String filePath) {
		return (!this.sshClientPassword.executeRemoteCommand(String.format("ls '%s'", filePath)).toString()
				.contains("No such file or directory")) || (this.sshClientPassword.isFileExists(filePath));
	}

	public void createFolderPath(final String folderPath) throws Exception {
		String createFolderPathCMD = String.format("mkdir -p '%s'", folderPath);
		this.executeRemoteCommand(createFolderPathCMD);
		if (!isFileExists(folderPath)) {
			throw new Exception("Folder creation gets failed!!!" + folderPath);
		}
	}

	public boolean findTextInFile(String remoteFilePath, String textToFind) {
		return this.sshClientPassword.findTextInFile(remoteFilePath, textToFind);
	}

	public Node getXMLNodeValueForGivenXpath(String remoteFilePath, String xpath) throws Exception {
		return this.sshClientPassword.getXMLNodeValueForGivenXpath(remoteFilePath, xpath);
	}

	public String getHomeDirectoryPath() {
		return this.sshClientPassword.getHomeDirPath();
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public Properties getConfig() {
		return this.config;
	}

	public void setConfig(final Properties config) {
		this.config = config;
	}

}
