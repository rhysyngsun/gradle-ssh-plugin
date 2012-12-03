package org.hidetake.gradle.ssh.integtest

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.session.ServerSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Helper class for an intergration test.
 * 
 * @author hidetake.org
 *
 */
class IntegrationTestHelper {
	final Logger logger = LoggerFactory.getLogger(IntegrationTestHelper)
	final SshServer server = SshServer.setUpDefaultServer()

	static final String DEFAULT_HOSTKEY = 'build/integration-test.host.key'

	/**
	 * Host key file of the SSH server.
	 * Default is {@value IntegrationTestHelper#DEFAULT_HOSTKEY}.
	 */
	String hostkey = DEFAULT_HOSTKEY

	boolean authenticatedByPassword = false
	List<String> requestedCommands = []

	IntegrationTestHelper() {
		server.host = 'localhost'
		server.port = Math.floor(Math.random() * 16384) + 16384
		server.keyPairProvider = new SimpleGeneratorHostKeyProvider(hostkey)
	}

	/**
	 * Enables password authentication.
	 * If credential did not match, it will cause an assertion failure.  
	 * 
	 * @param credential
	 */
	void enablePasswordAuthentication(Map credential) {
		server.passwordAuthenticator = [authenticate: { String username, String password, ServerSession s ->
				assertThat(username, is(credential.username))
				assertThat(password, is(credential.password))
				authenticatedByPassword = true
				true
			}] as PasswordAuthenticator
	}

	/**
	 * Enables command execution.
	 */
	void enableCommand() {
		server.commandFactory = [createCommand: { String command ->
				requestedCommands.add(command)
				new NullCommand(0)
			}] as CommandFactory
	}

	/**
	 * Execute the closure with the SSH server.
	 * 
	 * @param closure
	 */
	void execute(Closure closure) {
		server.start()
		logger.info("SSH server has been started at {}:{}", server.host, server.port)
		try {
			closure()
		} finally {
			server.stop()
			logger.info("SSH server has been terminated")
		}
	}
}
