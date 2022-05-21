package org.crucial.executor;

import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
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

import static org.testng.AssertJUnit.assertEquals;

@Test
public abstract class AbstractTest {

    protected Properties properties;
    protected ExecutorService service;

    public AbstractTest(){
        properties = System.getProperties();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(Config.CONFIG_FILE)) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        service = initService();
    }

    public abstract ExecutorService initService();

    public void nullTest() throws ExecutionException, InterruptedException {
        Future<Void> future =
                service.submit((Serializable & Callable<Void>) () -> {
                    return null;
                });
        assertEquals(future.get(),null);
    }

    public void intTest() throws InterruptedException, ExecutionException {
        Future<Integer> future =
                service.submit((Serializable & Callable<Integer>) () -> {
                    return 1;
                });
        assertEquals(future.get(),new Integer(1));
    }

    public void multipleTest() throws InterruptedException, ExecutionException {
        List<Future<Integer>> l =
                service.invokeAll(
                        IntStream.range(0,100).mapToObj(n ->
                                (Serializable & Callable<Integer>) () -> {
                                    return 1;
                                }).collect(Collectors.toList()));
        int count = 0;
        for (Future<Integer> f: l) {count+=f.get();}
        assertEquals(count,100);
    }

}
