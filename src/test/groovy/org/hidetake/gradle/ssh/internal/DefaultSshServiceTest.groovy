package org.hidetake.gradle.ssh.internal

import org.junit.Test

/**
 * Test cases for {@link DefaultSshService}.
 * 
 * @author hidetake.org
 *
 */
class DefaultSshServiceTest {
	@Test
	void retry_0_success() {
		int called = 0
		DefaultSshService.instance.retry(0) {
			called++
			assert called == 1
		}
		assert called == 1
	}

	@Test(expected = Exception)
	void retry_0_exception() {
		int called = 0
		DefaultSshService.instance.retry(0) {
			assert called == 0
			throw new Exception()
		}
	}

	@Test
	void retry_1_success() {
		int called = 0
		DefaultSshService.instance.retry(1) {
			called++
			assert called == 1
		}
		assert called == 1
	}

	@Test
	void retry_1_exceptionOnce() {
		int called = 0
		DefaultSshService.instance.retry(1) {
			called++
			if (called == 1) {
				throw new Exception('this should be handled by retry() method')
			}
			assert called == 2
		}
		assert called == 2
	}

	@Test(expected = Exception)
	void retry_1_exception2times() {
		int called = 0
		DefaultSshService.instance.retry(1) {
			called++
			assert (1..2).contains(called)
			if (called == 1) {
				throw new Exception('this exception should be handled by retry() method')
			}
			if (called == 2) {
				throw new Exception()
			}
		}
	}

	@Test
	void retry_2_success() {
		int called = 0
		DefaultSshService.instance.retry(2) { called++ }
		assert called == 1
	}

	@Test
	void retry_2_exceptionOnce() {
		int called = 0
		DefaultSshService.instance.retry(2) {
			called++
			assert (1..2).contains(called)
			if (called == 1) {
				throw new Exception('this exception should be handled by retry() method')
			}
		}
		assert called == 2
	}

	@Test
	void retry_2_exception2times() {
		int called = 0
		DefaultSshService.instance.retry(2) {
			called++
			assert (1..3).contains(called)
			if (called == 1) {
				throw new Exception('this exception should be handled by retry() method')
			}
			if (called == 2) {
				throw new Exception('this exception should be handled by retry() method')
			}
		}
		assert called == 3
	}

	@Test(expected = Exception)
	void retry_2_exception3times() {
		int called = 0
		DefaultSshService.instance.retry(2) {
			called++
			assert (1..3).contains(called)
			if (called == 1) {
				throw new Exception('this exception should be handled by retry() method')
			}
			if (called == 2) {
				throw new Exception('this exception should be handled by retry() method')
			}
			if (called == 3) {
				throw new Exception()
			}
		}
	}
}