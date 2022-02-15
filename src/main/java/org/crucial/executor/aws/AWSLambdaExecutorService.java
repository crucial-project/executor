package org.crucial.executor.aws;

import org.crucial.executor.Config;
import org.crucial.executor.ServerlessExecutorService;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Dictionary;
import java.util.Properties;

public class AWSLambdaExecutorService extends ServerlessExecutorService {

    private AWSLambdaInvoker invoker;
    private boolean logging;

    public AWSLambdaExecutorService() {
        Properties properties = System.getProperties();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(Config.CONFIG_FILE)) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        init(properties);
    }

    public AWSLambdaExecutorService(Properties properties ) {
        init(properties);
    }

    // FIXME
    private void init(Properties properties) {
        invoker = new AWSLambdaInvoker(properties);
        logging = Boolean.parseBoolean(properties.containsKey(Config.AWS_LAMBDA_LOGGING) ?
                properties.getProperty(Config.AWS_LAMBDA_LOGGING) : Config.AWS_LAMBDA_LOGGING_DEFAULT);
    }

    @Override
    protected byte[] invokeExternal(byte[] threadCall) {
        if (logging) System.out.println(this.printPrefix() + "Calling AWS Lambda.");
        InvokeResponse response = invoker.invoke(threadCall);
        assert response != null;
        if (logging) System.out.println(this.printPrefix() + "AWS call completed.");
        if (logging) { // FIXME not compatible w. -async.
            String log = new String(Base64.getDecoder().decode(response.logResult()));
            for(String line : log.split(System.getProperty("line.separator"))) {
                System.out.println(this.printPrefix() + line);
            }
        }
        return Base64.getMimeDecoder().decode(response.payload().asByteArray());
    }

    public void closeInvoker() {
        invoker.stop();
    }

    @Override
    public void deleteAllJobs () {}

    @Override
    public Dictionary<String, String> getServiceSpecs (String serviceName) { return null; }
}
