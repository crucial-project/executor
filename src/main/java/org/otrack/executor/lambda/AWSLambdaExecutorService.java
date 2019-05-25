package org.otrack.executor.lambda;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import org.otrack.executor.Config;
import org.otrack.executor.ServerlessExecutorService;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

import static org.otrack.executor.Json.fromJson;
import static org.otrack.executor.Json.toJson;
import static org.otrack.executor.Marshalling.*;

public class AWSLambdaExecutorService extends ServerlessExecutorService {

    private String region;
    private String arn;
    private AWSLambda client;

    public AWSLambdaExecutorService(Properties properties){
        this.region = properties.containsKey(Config.AWS_LAMBDA_REGION) ? properties.getProperty(Config.AWS_LAMBDA_REGION) : Config.AWS_LAMBDA_REGION_DEFAULT;
        this.arn = properties.containsKey(Config.AWS_LAMBDA_FUNCTION_ARN) ? properties.getProperty(Config.AWS_LAMBDA_FUNCTION_ARN) : Config.AWS_LAMBDA_FUNCTION_ARN_DEFAULT;
        this.client = AWSLambdaClientBuilder.standard()
                .withRegion(this.region)
                .withClientConfiguration(
                        new ClientConfiguration().withMaxConnections(1000)
                                .withConnectionTimeout(10 * 1000)
                                .withMaxErrorRetry(3))
                .build();
    }

    @Override
    public void shutdown() {
        // destroy function
    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return false;
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        try {
            return invokeAll(Collections.singleton(callable),0,TimeUnit.MILLISECONDS).get(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> Future<T> submit(Runnable runnable, T t) {
        return null;
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException {
        return invokeAll(collection,0,TimeUnit.MILLISECONDS);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException {
        List<Callable<T>> callables = Collections.synchronizedList(new ArrayList<>());
        collection.parallelStream().forEach(callable -> {
            callables.add(
                    (Callable<T>) () -> {
                        InvokeRequest inv = new InvokeRequest();
                        inv.setFunctionName(arn);
                        byte[] payload = toJson(toBytes(callable)).getBytes();
                        System.out.println("Invoking #"+invocationCounter.incrementAndGet()+": "+callable+" ["+payload.length+"]");
                        inv.setPayload(ByteBuffer.wrap(payload));
                        byte[] result = client.invoke(inv).getPayload().array();
                        byte[] result2 = Arrays.copyOfRange(result,1, result.length-1); // FIXME
                        System.out.println("Received ["+result.length+"]");
                        return (T) fromBytes(Base64.getDecoder().decode(result2));
                    });
        });
        return executorService.invokeAll(callables);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public void execute(Runnable runnable) {
        return;
    }

    //


}
