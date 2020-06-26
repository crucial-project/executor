package crucial.executor.k8s;

import crucial.executor.k8s.KubernetesExecutorService;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class Test {

    @org.testng.annotations.Test
    public void run(){
        KubernetesExecutorService service = new KubernetesExecutorService(1,"bash:3.0");
        service.submit((Serializable & Callable<Void>) () -> {
            System.out.println("hello world!");
            return null;
        });
    }

}
