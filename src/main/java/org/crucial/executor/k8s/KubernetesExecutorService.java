package org.crucial.executor.k8s;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.crucial.executor.ByteMarshaller;
import org.crucial.executor.ServerlessExecutorService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class KubernetesExecutorService extends ServerlessExecutorService {

    private KubernetesInvoker invoker;
    private final String jobName;
    private final String image;


    public KubernetesExecutorService(String jobName, String image) {
        this.jobName = jobName;
        this.image = image;
        init();
    }
    private void init() {
        invoker = new KubernetesInvoker(this.jobName, this.image);
    }

    @Override
    protected byte[] invokeExternal(byte[] input)  {
        debug(this.printPrefix() + "Calling k8s job.");
        String response = invoker.invoke(input, super.getListen(), super.getport(), super.getServiceName());
        assert response != null;
        debug(this.printPrefix() + "K8s call completed.");

        try {
            byte[] ret = ByteMarshaller.toBytes(response);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
