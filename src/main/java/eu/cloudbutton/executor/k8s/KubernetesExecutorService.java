package eu.cloudbutton.executor.k8s;

import eu.cloudbutton.executor.ServerlessExecutorService;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.BatchV1Api;
import io.kubernetes.client.models.V1Job;
import io.kubernetes.client.models.V1JobBuilder;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class KubernetesExecutorService extends ServerlessExecutorService {

    private int parallelism;
    private ApiClient apiClient;
    private String image;

    public KubernetesExecutorService(int parallelism, String image) {
        this.parallelism = parallelism;
        this.image = image;
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath("http://localhost:8080");
        this.apiClient.setDebugging(true);
    }

    @Override
    public void shutdown() {}

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
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        V1JobBuilder jobBuilder =
                new V1JobBuilder()
                        .withNewMetadata()
                        .withName("client")
                        .endMetadata()
                        .withNewSpec()
                        .withNewParallelism(this.parallelism)
                        .withNewTemplate()
                        .withNewSpec().withRestartPolicy("OnFailure")
                        .addNewContainer()
                        .withName("client").withImage(this.image)
                        .and().and().endTemplate().endSpec();
        // task.getClass()

        
        V1Job j = jobBuilder.build();
        BatchV1Api api1 = new BatchV1Api(apiClient);
        try {
            V1Job job = api1.createNamespacedJob("default",j,"false");
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return null;
    }

    @Override
    public Future<?> submit(Runnable task) {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public void execute(Runnable command) {

    }
}
