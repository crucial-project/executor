package org.crucial.executor.aws;

import org.crucial.executor.AbstractTest;
import org.testng.annotations.Test;

import java.awt.geom.PathIterator;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Test
public class BaseTest extends AbstractTest {

	public void nullTest() throws InterruptedException, ExecutionException {

		ExecutorService service = new AWSLambdaExecutorService();
		service.submit((Serializable & Callable<String>) () -> {
			return null;
		});
	}

	public void intTest() throws InterruptedException, ExecutionException {

		ExecutorService service = new AWSLambdaExecutorService();
		Future<Integer> future =
				service.submit((Serializable & Callable<Integer>) () -> {
					return 1;
				});
		assert future.get() == 1;
	}

	public void multipleTest() throws InterruptedException, ExecutionException {
		ExecutorService service = new AWSLambdaExecutorService();
		List<Future<Integer>> l =
                service.invokeAll(
                        IntStream.range(0,100).mapToObj(n ->
                                (Serializable & Callable<Integer>) () -> {
                                    return 1;
                                }).collect(Collectors.toList()));
		int count = 0;
		for (Future<Integer> f: l) {count+=f.get();}
		assert count == 100;
	}

}

