package com.itt.ssh;

import com.alibaba.ttl.TransmittableThreadLocal;

public class SshClientFactory {

	private static ThreadLocal<SshClient> sshClientFactory = new TransmittableThreadLocal<SshClient>();

	public static SshClient getSshClient() {
		return sshClientFactory.get();
	}

	public static void setSshClient(SshClient sshClient) {
		sshClientFactory.set(sshClient);
	}

}
