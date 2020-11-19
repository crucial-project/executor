 package org.crucial.executor.aws;

import org.crucial.executor.IterativeRunnable;
import org.crucial.executor.ServerlessExecutorService;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class AdvancedTest
{
    @Test
    public void testSubmit() throws ExecutionException, InterruptedException {
        final String ret = "test";

        ServerlessExecutorService es = new AWSLambdaExecutorService();
        es.setLocal(false);

        Future<String> future = es.submit((Serializable & Callable<String>) () -> {
            System.out.println("A");
            return ret;
        });

        assert future.get().equals(ret);

        Future<?> futureR = es.submit((Serializable & Runnable) () -> System.out.println("Called."));

        assert futureR.get() == null;

        List<Future<String>> futures = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            futures.add(es.submit((Serializable & Callable<String>) () -> {
                System.out.println("Run. " + finalI);
                return ret;
            }));
        }
        futures.forEach(stringFuture -> {
            try {
                stringFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void testInvokeAll() throws ExecutionException, InterruptedException {
        final String ret = "test";

        ServerlessExecutorService es = new AWSLambdaExecutorService();
        es.setLocal(false);
	
        List<Callable<String>> myTasks = Collections.synchronizedList(new ArrayList<>());
        IntStream.range(0, 1).forEach(i ->
                myTasks.add((Serializable & Callable<String>) () -> {
                    System.out.println("Run." + i);
                    return ret;
                }));
        List<Future<String>> futures = es.invokeAll(myTasks);
        for (Future<String> future : futures) {
            assert future.get().equals(ret);
        }
    }

    @Test
    public void testInvokeIterativeTask() {
        ServerlessExecutorService es = new AWSLambdaExecutorService();
        es.setLocal(false);

        System.out.println("Executor:");
        try {
            es.invokeIterativeTask((IterativeRunnable) index -> System.out.println("Index " + index),
                    2, 0, 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("With finalize:");
        try {
            es.invokeIterativeTask(
                    (IterativeRunnable) index -> System.out.println("Index " + index),
                    2, 0, 10,
                    (Serializable & Runnable) () -> System.out.println("Over"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
