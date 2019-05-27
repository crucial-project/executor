package eu.cloudbutton.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ServerlessExecutorService implements ExecutorService {

    protected ExecutorService executorService;
    protected AtomicInteger invocationCounter;

    public ServerlessExecutorService(){
        executorService = Executors.newFixedThreadPool(1000);
        invocationCounter = new AtomicInteger();
    }
}
