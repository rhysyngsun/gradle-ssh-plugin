package org.hidetake.gradle.ssh.integtest

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.SshTask
import org.junit.Test

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
}
