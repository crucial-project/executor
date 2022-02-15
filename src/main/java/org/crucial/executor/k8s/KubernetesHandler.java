package org.crucial.executor.k8s;

import org.crucial.executor.ByteMarshaller;
import org.crucial.executor.CloudThreadHandler;
import org.crucial.executor.Json;

import java.io.IOException;

public class KubernetesHandler extends CloudThreadHandler{

    public static void main(String[] args) {
        KubernetesHandler handler = new KubernetesHandler();


        // decode Base64 String to byte array
        //byte[] decode = Base64.getDecoder().decode(args[0]);
        byte[] decode = Json.fromJson(args[0]);

        // Handle the threadcall
        byte [] output = handler.handle(decode);

        // convert the returned object to String
        String str = null;
        try {
            str = ByteMarshaller.fromBytes(output);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println(str);


    }
}


