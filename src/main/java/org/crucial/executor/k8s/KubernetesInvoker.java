package org.crucial.executor.k8s;

import java.util.Arrays;
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
    private final String image;
    private KubernetesService service;

    public KubernetesInvoker(String jobName, String image) {
        this.jobName = jobName;
        this.image = image;
    }

    public String invoke(byte[] input, boolean listen, int port, String serviceName) {

        String joblog = null;

        String myName = jobName + "-" + Thread.currentThread().getName();

        String selectorKey = "job";
        String selectorValue = myName;


        String mainClass = "org.crucial.executor.k8s.KubernetesHandler";
        String libs = "/usr/local/executor.jar:/usr/local/executor-tests.jar:/usr/local/lib/*:.";
        List<String> command = Arrays.asList("java","-classpath", libs, mainClass, Json.toJson(input));

        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.withOauthToken("eyJhbGciOiJSUzI1NiIsImtpZCI6ImlzXzhOSjdGYjFlT3NGdU9jRWpQM3M0bWxHazl0Uzc1bUdKMnVGM0hPaWMifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImRlZmF1bHQtdG9rZW4tcXRoNXoiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZGVmYXVsdCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjNkMDdhOTIwLTAyNDYtNDUxNi05ZDQ1LTFhN2Y5NDE3NTljMiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmRlZmF1bHQifQ.rVKd9V492fqF1JRPdH0cE8X8bFannRn9spzNq0r2lL9TIHLeOsXzbrm8XHhOrIBLjJhA9YLifk8ULMr1B7lZ3QivB4Tw1oPK4qYPTp3n_ntD_bEXmDzSDo66MfzE7MRgiHO8TIcPNuBKQ3RBPbf1edB7ZqwRd-jkGhgGH0qHcPChoOfxN8TUb5T4RGlqpFZ9NFxyfeScqcxQHwtdSrp7rGz5HAMrIlb6Zra39O1IQ66vYziCAFj_dVyQ66TlRS-NF0I2-mgGM8TS2yB6sdz3f2JvE8xdjETZCEFt9atg3JaNeQYGue0d0M9Gv6ss7BGLqyl37EwYpWrg_5LB3BAyvw");
        try (KubernetesClient client = new DefaultKubernetesClient(configBuilder.build())) {
            final String namespace = "default";
            final Job job = new JobBuilder()
                    .withApiVersion("batch/v1")
                    .withNewMetadata()
                    .withName(myName)
                    .addToLabels("name", myName)
                    .endMetadata()
                    .withNewSpec()
                    .withNewTemplate()
                    .withNewMetadata()
                    .addToLabels(selectorKey, selectorValue)
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainer()
                    .withName(myName)
                    .withImage(image)
                    .withCommand(command)
                    .endContainer()
                    .withRestartPolicy("Never")
                    .endSpec()
                    .endTemplate()
                    .endSpec()
                    .build();

            System.out.println("Creating job " + myName);
            client.batch().v1().jobs().inNamespace(namespace).createOrReplace(job);

            if (listen) {
                // Create the service
                service = new KubernetesService();
                service.start(serviceName, selectorKey, selectorValue, port);
            }

            Thread.sleep(1000);
            // Get All pods created by the job
            PodList podList = client.pods().inNamespace(namespace).withLabel("job-name", job.getMetadata().getName()).list();

            // Wait for pod to complete
            client.pods().inNamespace(namespace).withName(podList.getItems().get(0).getMetadata().getName())
                    .waitUntilCondition(pod -> pod.getStatus().getPhase().equals("Succeeded"), 2, TimeUnit.MINUTES);

            // Print Job's log
            joblog = client.pods().inNamespace(namespace).withName(podList.getItems().get(0).getMetadata().getName()).getLog();

            return joblog;

        } catch (KubernetesClientException | InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }

    }
}


