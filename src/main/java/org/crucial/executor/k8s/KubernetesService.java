package org.crucial.executor.k8s;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;


import java.util.Collections;
import java.util.Optional;

public class KubernetesService {

    public static void start(String name, String selectorKey, String selectorValue, int port) {
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            String namespace = Optional.ofNullable(client.getNamespace()).orElse("default");

            Service service = new ServiceBuilder()
                    .withNewMetadata()
                    .withName(name)
                    .endMetadata()
                    .withNewSpec()
                    .withSelector(Collections.singletonMap(selectorKey, selectorValue))
                    .addNewPort()
                    .withName("test-port")
                    .withProtocol("TCP")
                    .withPort(port)
                    //.withTargetPort(new IntOrString(8080))
                    .endPort()
                    .withType("LoadBalancer")
                    .endSpec()
                    .build();

            client.services().inNamespace(namespace).createOrReplace(service);
        }
    }
}
