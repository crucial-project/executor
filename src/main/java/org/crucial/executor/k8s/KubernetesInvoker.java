package org.crucial.executor.k8s;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;



import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.crucial.executor.Json;

public class KubernetesInvoker {

    private String jobName;
    private String image;
    private String token;

    private KubernetesService service;

    public KubernetesInvoker(String jobName, String image, String token) {
        this.jobName = jobName;
        this.image = image;
        this.token = token;
    }

    public byte[] invoke(byte[] input, boolean listen, int port, String serviceName) {
        String myName = jobName + "-" + Thread.currentThread().getName();
        myName = jobName;

        String selectorKey = "job";
        String selectorValue = myName;

        ConfigBuilder configBuilder = new ConfigBuilder();

        configBuilder.withOauthToken(token);

        try{
            KubernetesClient client = new DefaultKubernetesClient(configBuilder.build());
            final String namespace = "default";
            final Job job = new JobBuilder()
                    .withApiVersion("batch/v1")
                    .withNewMetadata()
                    .withName(myName)
//                    .addToLabels("name", myName)
                    .endMetadata()
                    .withNewSpec()
                    .withNewTemplate()
                    .withNewMetadata()
                    .addToLabels(selectorKey, selectorValue)
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainer()
                    .withImagePullPolicy("Always")
                    .withName("a")
                    .withImage(image)
                    .addNewEnv()
                    .withName("INPUT")
                    .withValue(Json.toJson(input))
                    .endEnv()
                    //.withCommand(command)
                    .addNewEnv()
                    .withName("JVM_EXTRA")
                    .withValue("")
                    .endEnv()
                    .endContainer()
                    .withRestartPolicy("Never")
                    .endSpec()
                    .endTemplate()
                    .endSpec()
                    .build();

            assert client.batch().v1().jobs().inNamespace(namespace) != null;

            client.batch().v1().jobs().inNamespace(namespace).createOrReplace(job);

            if (listen) {
                // Create the service
                service = new KubernetesService();
                service.start(serviceName, selectorKey, selectorValue, port);
            }

            PodList podList;
            do {
                 podList = client.pods().inNamespace(namespace).withLabel("job-name", job.getMetadata().getName()).list();
            } while(podList.getItems().isEmpty());

            client.pods().inNamespace(namespace).withName(podList.getItems().get(0).getMetadata().getName())
                    .waitUntilCondition(pod -> pod.getStatus().getPhase().equals("Succeeded"), 2, TimeUnit.MINUTES);

            return Json.fromJson(client.pods().inNamespace(namespace).withName(podList.getItems().get(0).getMetadata().getName()).getLog());

        } catch (KubernetesClientException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }

    }

}


