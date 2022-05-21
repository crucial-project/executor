package org.crucial.executor.aws;

import org.crucial.executor.Config;
import org.crucial.executor.Json;
import org.crucial.executor.ServerlessExecutorService;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

import java.time.Duration;
import java.util.Base64;
import java.util.Dictionary;

public class AWSLambdaExecutorService extends ServerlessExecutorService {

    private AWSLambdaInvoker invoker;
    private boolean asynchronous;
    private LambdaClient lambdaClient;
    private String region;
    private String arn;
    private int timeout;

    public AWSLambdaExecutorService() {
        region = properties.containsKey(Config.AWS_LAMBDA_REGION) ?
                properties.getProperty(Config.AWS_LAMBDA_REGION) : Config.AWS_LAMBDA_REGION_DEFAULT;
        arn = properties.containsKey(Config.AWS_LAMBDA_FUNCTION_ARN) ?
                properties.getProperty(Config.AWS_LAMBDA_FUNCTION_ARN) : Config.AWS_LAMBDA_FUNCTION_ARN_DEFAULT;
        asynchronous |= Boolean.parseBoolean(properties.containsKey(Config.AWS_LAMBDA_FUNCTION_ASYNC) ?
                properties.getProperty(Config.AWS_LAMBDA_FUNCTION_ASYNC) : Config.AWS_LAMBDA_FUNCTION_ASYNC_DEFAULT);
        timeout |= Integer.parseInt(properties.containsKey(Config.AWS_LAMBDA_CLIENT_TIMEOUT) ?
                properties.getProperty(Config.AWS_LAMBDA_CLIENT_TIMEOUT) : Config.AWS_LAMBDA_CLIENT_TIMEOUT_DEFAULT);
        invoker = new AWSLambdaInvoker(properties);
        lambdaClient = LambdaClient.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder().socketTimeout(Duration.ofSeconds(timeout)))
                .region(Region.of(region))
                .build();
    }

    @Override
    protected byte[] invokeExternal(byte[] threadCall) {
        debug("Calling AWS Lambda.");

        // Invoke
        GetFunctionRequest gf = GetFunctionRequest.builder().functionName(arn).build();
        lambdaClient.getFunction(gf);

        InvokeRequest.Builder requestTuilder = InvokeRequest.builder();
        requestTuilder.functionName(arn);
        if (asynchronous) {
            requestTuilder.invocationType(InvocationType.EVENT);
        } else {
            requestTuilder.invocationType(InvocationType.REQUEST_RESPONSE);
        }
        requestTuilder.payload(SdkBytes.fromByteArray(Json.toJson(threadCall).getBytes()));
        if (debug) requestTuilder.logType(LogType.TAIL);

        // Response
        InvokeResponse response = lambdaClient.invoke(requestTuilder.build());
        assert response != null;
        if (debug) {
            debug("AWS call completed.");
            if (!asynchronous) {
                String s = new String(Base64.getDecoder().decode(response.logResult()));
                for (String line : s.split(System.getProperty("line.separator"))) {
                    debug(line);
                }
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
