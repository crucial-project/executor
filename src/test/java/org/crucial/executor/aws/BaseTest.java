package org.crucial.executor.aws;

import org.crucial.executor.AbstractTest;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;

@Test
public class BaseTest extends AbstractTest {

	@Override
	public ExecutorService initService() {
		return new AWSLambdaExecutorService();
	}
}

