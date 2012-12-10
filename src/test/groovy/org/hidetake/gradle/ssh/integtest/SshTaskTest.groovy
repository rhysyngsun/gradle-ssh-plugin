package org.hidetake.gradle.ssh.integtest

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import java.security.PublicKey

import org.apache.sshd.server.session.ServerSession
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.SshTask
import org.junit.Test

/**
 * Test cases for {@link SshTask}.
 * Factors:
 * <ul>
 * <li>Authentication method: password, publickey</li>
 * <li>Channel kind: exec, sftp</li>
 * <li>Settings: global, task-specific</li>
 * </ul>
 * 
 * @author hidetake
 *
 */
class SshTaskTest {
	@Test
	void testPasswordAuthentication() {
		def helper = new IntegrationTestHelper()
		helper.enablePasswordAuthentication(username: 'someuser', password: 'somepassword')
		helper.enableCommand()
		helper.execute {
			def project = ProjectBuilder.builder().build()
			project.with {
				apply plugin: 'ssh'
				ssh { config(StrictHostKeyChecking: 'no') }
				remotes {
					webServer {
						host = helper.server.host
						port = helper.server.port
						user = 'someuser'
						password = 'somepassword'
					}
				}
				task(type: SshTask, 'testTask') {
					session(remotes.webServer) { execute 'ls' }
				}
			}
			project.tasks.testTask.execute()
		}
		assert helper.authenticatedByPassword == true
		assert helper.requestedCommands.contains('ls')
	}

	@Test
	void testPublickeyAuthentication() {
		def identityFile = new File(SshTaskTest.getResource('/id_rsa').file)
		assert identityFile != null, 'could not load test fixture'
		def helper = new IntegrationTestHelper()
		helper.enablePublickeyAuthentication { String username, PublicKey key, ServerSession s ->
			assert username == 'someuser'
			assert key.algorithm == 'RSA'
		}
		helper.enableCommand()
		helper.execute {
			def project = ProjectBuilder.builder().build()
			project.with {
				apply plugin: 'ssh'
				ssh { config(StrictHostKeyChecking: 'no') }
				remotes {
					webServer {
						host = helper.server.host
						port = helper.server.port
						user = 'someuser'
						identity = identityFile
					}
				}
				task(type: SshTask, 'testTask') {
					session(remotes.webServer) { execute 'ls' }
				}
			}
			project.tasks.testTask.execute()
		}
		assert helper.authenticatedByPassword == true
		assert helper.requestedCommands.contains('ls')
	}
}
