package org.crucial.executor.aws;


import com.amazonaws.services.lambda.model.InvokeResult;
import org.crucial.executor.ServerlessExecutorService;
import org.crucial.executor.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

public class AWSLambdaExecutorService extends ServerlessExecutorService {
    private final AWSLambdaInvoker invoker;

    public AWSLambdaExecutorService() {
        Properties properties = System.getProperties();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(Config.CONFIG_FILE)) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        invoker = new AWSLambdaInvoker(properties);
    }

    @Override
    protected byte[] invokeExternal(byte[] threadCall) {
        System.out.println(this.printPrefix() + "Calling AWS Lambda.");
        InvokeResult result = invoker.invoke(threadCall);
        System.out.println(this.printPrefix() + "AWS call completed.");
        if (logs) {
            System.out.println(this.printPrefix() + "Showing Lambda Tail Logs.\n");
            assert result != null;
            System.out.println(new String(Base64.getDecoder().decode(result.getLogResult())));
        }
        return Base64.getMimeDecoder().decode(result.getPayload().array());
    }

    public void closeInvoker() {
        invoker.stop();
    }
}
