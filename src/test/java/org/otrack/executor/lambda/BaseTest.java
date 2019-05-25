package org.otrack.executor.lambda;

import org.otrack.executor.AbstractTest;
import org.otrack.executor.Config;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Test
public class BaseTest extends AbstractTest {

    @Test
    public void helloWorld() throws ExecutionException, InterruptedException {
        final String welcome = "Hello World!";
        AWSLambdaExecutorService service = new AWSLambdaExecutorService(properties);
        Future<String> future = service.submit((Serializable & Callable<String>) () -> {
            System.out.println(welcome);
            return welcome;
        });
        assert future.get().equals(welcome);
    }

}
