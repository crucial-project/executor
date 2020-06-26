package crucial.executor.k8s;

import crucial.executor.ServerlessExecutorService;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.BatchV1Api;
import io.kubernetes.client.models.V1Job;
import io.kubernetes.client.models.V1JobBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

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
            V1Job job = api1.createNamespacedJob("default", j, "false");
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected byte[] invokeExternal(byte[] threadCall) {
        return new byte[0];
    }

    @Override
    public void closeInvoker() {

    }

}
