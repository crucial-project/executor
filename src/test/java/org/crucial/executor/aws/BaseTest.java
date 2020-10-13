package org.crucial.executor.aws;

import org.crucial.executor.AbstractTest;
import org.testng.annotations.Test;

import java.awt.geom.PathIterator;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Test
public class BaseTest extends AbstractTest {
    
    public void nullTest() throws InterruptedException, ExecutionException {

	ExecutorService service = new AWSLambdaExecutorService();
	service.submit((Serializable & Callable<String>) () -> {
		return null;
	    });
	
    }

}

