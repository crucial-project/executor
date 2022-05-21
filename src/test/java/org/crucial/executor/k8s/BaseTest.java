package org.crucial.executor.k8s;

import org.crucial.executor.AbstractTest;

import java.util.concurrent.ExecutorService;

public class BaseTest extends AbstractTest {

    @Override
    public ExecutorService initService() {
        return new KubernetesExecutorService();
    }
}
