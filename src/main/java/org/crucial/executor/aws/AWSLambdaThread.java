package org.crucial.executor.aws;

import org.crucial.executor.CloudThread;
import org.crucial.executor.Config;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

public class AWSLambdaThread extends CloudThread {

    private static AWSLambdaInvoker invoker;

    public AWSLambdaThread(Runnable target) {
        super(target);
        Properties properties = System.getProperties();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(Config.CONFIG_FILE)) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        invoker = new AWSLambdaInvoker(properties);
        invoker.initClient();
    }

    @Override
    protected void invoke(byte[] threadCall) {
        System.out.println(this.printPrefix() + "Calling AWS Lambda.");
        InvokeResponse response = invoker.invoke(threadCall);
        System.out.println(this.printPrefix() + "AWS call completed.");
        if (logs) {
            System.out.println(this.printPrefix() + "Showing Lambda Tail Logs.\n");
            assert response != null;
            System.out.println(new String(Base64.getDecoder().decode(response.logResult())));
        }
    }

    public static void closeInvoker(){
        invoker.stop();
    }
}
