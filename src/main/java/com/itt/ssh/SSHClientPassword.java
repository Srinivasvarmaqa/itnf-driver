package com.itt.ssh;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SSHClientPassword extends SshClient {

	private static final Logger LOG = LoggerFactory.getLogger(SSHClientPassword.class);

	private String sshClienthost;
	private String sshClientpassword;
	private String sshClientuser;
	private String sshClienthomeDirPath;
	private Properties config = new Properties();

	public SSHClientPassword(final String remoteHost, final String remoteHostPassword, final String user) {
		this.sshClientuser = user;
		this.sshClienthost = remoteHost;
		this.sshClientpassword = remoteHostPassword;
		this.config.put("StrictHostKeyChecking", "no");
		this.setHomeDirPath();
	}

	/**
	 * Executes the specified command.
	 *
	 * @param command
	 * @return list of messages from remote server.
	 */
	public List<String> executeRemoteCommand(final String command) {

		final List<String> messageList = new ArrayList<String>();

		Session session = null;
		Channel channel = null;
		try {
			LOG.debug("COMMAND : " + command);

			final JSch jsch = new JSch();
			session = jsch.getSession(this.sshClientuser, this.sshClienthost);
			session.setPassword(this.sshClientpassword);
			session.setConfig(this.config);

			session.connect();

			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			Thread.sleep(30);
			final InputStream input = channel.getInputStream();
			final InputStream errorStream = ((ChannelExec) channel).getErrStream();
			channel.connect();

			LOG.debug("CHANNEL CONNECTED TO MACHINE " + this.sshClienthost + " SERVER WITH COMMAND: " + command);
			InputStreamReader inputReader = null,errinputReader = null;
			BufferedReader bufferedReader = null,errbufferedReader = null;
			
			try {
				inputReader = new InputStreamReader(input);
				bufferedReader = new BufferedReader(inputReader);
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					messageList.add(line);
				}
				
				errinputReader = new InputStreamReader(errorStream);
				errbufferedReader = new BufferedReader(errinputReader);
				String errline = null;
				while ((errline = errbufferedReader.readLine()) != null) {
					messageList.add(errline);
				}
				
			} catch (final IOException ex) {
				ex.printStackTrace();
			}finally
			{
				if(inputReader!=null)
				{
					inputReader.close();
				}
				if(bufferedReader!=null)
				{
					bufferedReader.close();
				}
				if(errinputReader!=null)
				{
					errinputReader.close();
				}
				if(errbufferedReader!=null)
				{
					errbufferedReader.close();
				}
				
			}

		} catch (final Exception ex) {
			ex.printStackTrace();
		} finally {
			if (channel != null) {
				channel.disconnect();
			}

			if (session != null) {
				session.disconnect();
			}
		}
		LOG.debug(messageList.toString());
		return messageList;
	}
	
	
	
	/**
	 * Executes the specified command.
	 *
	 * @param command
	 * @return list of messages from remote server.
	 */
	public void launchNodeJSService(final String command) {
		Session session = null;
		Channel channel = null;
		try {
			LOG.info("COMMAND : " + command);

			final JSch jsch = new JSch();
			session = jsch.getSession(this.sshClientuser, this.sshClienthost);
			session.setPassword(this.sshClientpassword);
			session.setConfig(this.config);
			session.connect();
			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			Thread.sleep(30);
			final InputStream input = channel.getInputStream();
			channel.connect();
			LOG.info("CHANNEL CONNECTED TO MACHINE " + this.sshClienthost + " SERVER WITH COMMAND: " + command);
			
		}catch(final com.jcraft.jsch.JSchException jschexception)
		{
			LOG.info("ERROR OCCURED WHILE LAUNCHING NODE JS SERVICE!!!" );
			jschexception.printStackTrace();
		}
		catch (final Exception ex) {
			LOG.info("ERROR OCCURED WHILE LAUNCHING NODE JS SERVICE!!!" );
			ex.printStackTrace();
		} 
	}
	

	public String getHomeDirPath() {
		return this.sshClienthomeDirPath;
	}

	public boolean isFileExists(String remoteFilePath) {
		Session session = null;
		ChannelSftp sftp = null;
		Vector res = null;
		try {
			final JSch jsch = new JSch();
			session = jsch.getSession(this.sshClientuser, this.sshClienthost);
			session.setPassword(this.sshClientpassword);
			session.setConfig(this.config);
			session.connect();
			sftp = (ChannelSftp) session.openChannel("sftp");
			sftp.connect();
			res = sftp.ls(remoteFilePath);
			return res != null && !res.isEmpty();
		} catch (final Exception ex) {
			ex.printStackTrace();
		} finally {
			if (sftp != null) {
				sftp.disconnect();
			}

			if (session != null) {
				session.disconnect();
			}
		}
		return false;
	}

	public void copyFileFrom(String remoteFilePath, String localFilePath) {
		Session session = null;
		ChannelSftp sftp = null;
		try {
			final JSch jsch = new JSch();
			session = jsch.getSession(this.sshClientuser, this.sshClienthost);
			session.setPassword(this.sshClientpassword);
			session.setConfig(this.config);
			session.connect();
			sftp = (ChannelSftp) session.openChannel("sftp");
			sftp.connect();
			
			sftp.get(remoteFilePath, localFilePath);
		} catch (final Exception ex) {
			ex.printStackTrace();
			LOG.error("File Copy is unsuccessful: ", remoteFilePath);
		} finally {
			if (sftp != null) {
				sftp.disconnect();
			}

			if (session != null) {
				session.disconnect();
			}
		}
	}
	
	
	public void copyFileFromLocalToRemote(String remoteFilePath, String localFilePath) {
		Session session = null;
		ChannelSftp sftp = null;
		try {
			final JSch jsch = new JSch();
			session = jsch.getSession(this.sshClientuser, this.sshClienthost);
			session.setPassword(this.sshClientpassword);
			session.setConfig(this.config);
			session.connect();
			sftp = (ChannelSftp) session.openChannel("sftp");
			sftp.connect();
			
			FileInputStream fis = null;
			sftp.cd(remoteFilePath);

			// Upload file
			File file = new File(localFilePath);
			fis = new FileInputStream(file);
			sftp.put(fis, file.getName());

			fis.close();
		} catch (final Exception ex) {
			ex.printStackTrace();
			LOG.error("File Copy is unsuccessful: ", remoteFilePath);
		} finally {
			if (sftp != null) {
				sftp.disconnect();
			}

			if (session != null) {
				session.disconnect();
			}
		}
	}
		
	public boolean findTextInFile(String remoteFilePath, String textToFind) {
		Session session = null;
		ChannelSftp sftp = null;
		Vector res = null;
		boolean result = false;
		try {
			final JSch jsch = new JSch();
			session = jsch.getSession(this.sshClientuser, this.sshClienthost);
			session.setPassword(this.sshClientpassword);
			session.setConfig(this.config);
			session.connect();
			sftp = (ChannelSftp) session.openChannel("sftp");
			sftp.connect();
			InputStream stream = sftp.get(remoteFilePath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String outputdata = "";
			while ((outputdata = reader.readLine()) != null) {
				if (outputdata.contains(textToFind)) {
					result = true;
					break;
				}
			}

		} catch (final Exception ex) {
			ex.printStackTrace();
			return result;
		} finally {
			if (sftp != null) {
				sftp.disconnect();
			}

			if (session != null) {
				session.disconnect();
			}
		}
		return result;

	}
	
	public String getFileContent(String remoteFilePath) throws Exception
	{
		List<String> fileContent = new ArrayList<String>();
		Session session = null;
		ChannelSftp sftp = null;
		InputStream stream = null;
		BufferedReader reader = null;
		try {
			final JSch jsch = new JSch();
			session = jsch.getSession(this.sshClientuser, this.sshClienthost);
			session.setPassword(this.sshClientpassword);
			session.setConfig(this.config);
			session.connect();
			sftp = (ChannelSftp) session.openChannel("sftp");
			sftp.connect();
			stream = sftp.get(remoteFilePath);
			reader = new BufferedReader(new InputStreamReader(stream));
			String outputdata = "";
			while ((outputdata = reader.readLine()) != null) {
				fileContent.add(outputdata);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		} finally {
			if (sftp != null) {
				sftp.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
			if (reader != null) {
				reader.close();

			}
			if (stream != null) {
				stream.close();
			}
		}

		return fileContent.toString();
	}
	
	public Node getXMLNodeValueForGivenXpath(String remoteFilePath, String xpath) throws Exception
	{
		Session session = null;
		ChannelSftp sftp = null;
		Vector res = null;
		boolean result = false;
		Node node = null;
		try {
			
			final JSch jsch = new JSch();
			session = jsch.getSession(this.sshClientuser, this.sshClienthost);
			session.setPassword(this.sshClientpassword);
			session.setConfig(this.config);
			session.connect();
			sftp = (ChannelSftp) session.openChannel("sftp");
			sftp.connect();
			InputStream stream = sftp.get(remoteFilePath);
			InputSource inputfile = new InputSource(stream);
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputfile);
			XPath xpathVariable = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList) xpathVariable.evaluate(xpath, doc, XPathConstants.NODESET);
			if(nodes.getLength()>0)
				return nodes.item(0);
			else
				return node;
		}catch (final Exception ex) {
			ex.printStackTrace();
			return node;
		} finally {
			if (sftp != null) {
				sftp.disconnect();
			}

			if (session != null) {
				session.disconnect();
			}
		}
		
	}

	public String getHost() {
		return this.sshClienthost;
	}

	public void setHost(final String host) {
		this.sshClienthost = host;
	}

	public String getPassword() {
		return this.sshClientpassword;
	}

	public void setPassword(final String password) {
		this.sshClientpassword = password;
	}

	public String getUser() {
		return this.sshClientuser;
	}

	public void setUser(final String user) {
		this.sshClientuser = user;
	}

	public Properties getConfig() {
		return this.config;
	}

	public void setConfig(final Properties config) {
		this.config = config;
	}

	public void setHomeDirPath() {
		Session session = null;
		Channel channel = null;
		try {
			final JSch jsch = new JSch();
			session = jsch.getSession(this.sshClientuser, this.sshClienthost);
			session.setPassword(this.sshClientpassword);
			session.setConfig(this.config);
			session.connect();
			ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
			sftp.connect();
			this.sshClienthomeDirPath = sftp.getHome().toString().trim();
		} catch (final Exception ex) {
			ex.printStackTrace();
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
