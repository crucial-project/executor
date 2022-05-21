package org.crucial.executor.k8s;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.crucial.executor.ByteMarshaller;
import org.crucial.executor.Config;
import org.crucial.executor.ServerlessExecutorService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class KubernetesExecutorService extends ServerlessExecutorService {

    private final KubernetesInvoker invoker;
    private final String jobName;
    private final String image;
    private final String token;

    public KubernetesExecutorService() {
        jobName = properties.containsKey(Config.K8S_JOB_NAME) ?
                properties.getProperty(Config.K8S_JOB_NAME) : Config.K8S_JOB_NAME_DEFAULT;
        image = properties.containsKey(Config.K8S_IMAGE) ?
                properties.getProperty(Config.K8S_IMAGE) : Config.K8S_IMAGE_DEFAULT;
        token = properties.containsKey(Config.K8S_TOKEN) ?
                properties.getProperty(Config.K8S_TOKEN) : Config.K8S_TOKEN_DEFAULT;

        invoker = new KubernetesInvoker(this.jobName, this.image, this.token);
    }

    @Override
    protected byte[] invokeExternal(byte[] threadCall)  {
        debug("Calling k8s job.");
        byte[] response = invoker.invoke(threadCall, super.getListen(), super.getport(), super.getServiceName());
        assert response != null;
        debug("K8s call completed.");
        return response;
    }

    @Override
    public void closeInvoker() {

    }

    @Override
    public void deleteAllJobs() {

        try (KubernetesClient client = new DefaultKubernetesClient()) {
            String namespace = Optional.ofNullable(client.getNamespace()).orElse("default");

            // Get All jobs in Namespace
            List<Job> jobList = client.batch().v1().jobs().inNamespace(namespace).list().getItems();

            // Delete job
            client.batch().v1().jobs().inNamespace(namespace).delete(jobList);
        }
    }


    @Override
    public Dictionary<String, String> getServiceSpecs(String serviceName) {

        try (KubernetesClient client = new DefaultKubernetesClient()) {
            String namespace = Optional.ofNullable(client.getNamespace()).orElse("default");

            // Get the service
            Service service = null;
            while (service == null){
                service = client.services().inNamespace(namespace).withName(serviceName).get();
            }

            // Wait until the External IP is ready
            client.services().inNamespace(namespace).withName(serviceName).waitUntilCondition(serv ->  serv.getStatus().getLoadBalancer().getIngress().size() > 0 , 2, TimeUnit.MINUTES);

            // Get the IP and Port
            String serviceIP = client.services().inNamespace(namespace).withName(serviceName).get().getStatus().getLoadBalancer().getIngress().get(0).getIp();
            Integer port = service.getSpec().getPorts().get(0).getPort();

            // Create and return the dictionary
            Dictionary<String, String> serviceSpecs = new Hashtable<>();
            serviceSpecs.put("IP", serviceIP);
            serviceSpecs.put("port", port.toString());

            return serviceSpecs;
        }
    }



}
