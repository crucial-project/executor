package org.crucial.executor.k8s;

import org.crucial.executor.ByteMarshaller;
import org.crucial.executor.CloudThreadHandler;
import org.crucial.executor.Json;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

public class KubernetesHandler extends CloudThreadHandler{

    public static void main(String[] args) {
        KubernetesHandler handler = new KubernetesHandler();
        byte[] input = Json.fromJson(args[0]);
        byte [] output = handler.handle(input);
        System.out.print(Json.toJson(output));
        System.out.flush();
    }
}
