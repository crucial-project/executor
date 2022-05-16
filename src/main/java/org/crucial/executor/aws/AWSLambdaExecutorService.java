package org.crucial.executor.aws;

import org.crucial.executor.Config;
import org.crucial.executor.Json;
import org.crucial.executor.ServerlessExecutorService;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Dictionary;
import java.util.Properties;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static org.crucial.executor.Config.*;

public class AWSLambdaExecutorService extends ServerlessExecutorService {

    private AWSLambdaInvoker invoker;
    private Properties properties = System.getProperties();
    private boolean asynchronous;
    private boolean debug;
    private LambdaClient lambdaClient;
    private String region;
    private String arn;
    private int timeout;

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

    private void init(Properties properties) {
        Path path = Paths.get(Config.CONFIG_FILE);
        try {
            InputStream is = new ByteArrayInputStream(Files.readAllBytes(path));
            properties.load(is);
            region = properties.containsKey(Config.AWS_LAMBDA_REGION) ?
                    properties.getProperty(Config.AWS_LAMBDA_REGION) : Config.AWS_LAMBDA_REGION_DEFAULT;
            arn = properties.containsKey(Config.AWS_LAMBDA_FUNCTION_ARN) ?
                    properties.getProperty(Config.AWS_LAMBDA_FUNCTION_ARN) : Config.AWS_LAMBDA_FUNCTION_ARN_DEFAULT;
            debug |= Boolean.parseBoolean(properties.containsKey(Config.AWS_LAMBDA_DEBUG) ?
                    properties.getProperty(Config.AWS_LAMBDA_DEBUG) : Config.AWS_LAMBDA_DEBUG_DEFAULT);
            asynchronous |= Boolean.parseBoolean(properties.containsKey(Config.AWS_LAMBDA_FUNCTION_ASYNC) ?
                    properties.getProperty(Config.AWS_LAMBDA_FUNCTION_ASYNC) : Config.AWS_LAMBDA_FUNCTION_ASYNC_DEFAULT);
            timeout |= Integer.parseInt(properties.containsKey(Config.AWS_LAMBDA_CLIENT_TIMEOUT) ?
                    properties.getProperty(Config.AWS_LAMBDA_CLIENT_TIMEOUT) : Config.AWS_LAMBDA_CLIENT_TIMEOUT_DEFAULT);
            invoker = new AWSLambdaInvoker(properties);
            lambdaClient = LambdaClient.builder()
                    .httpClientBuilder(UrlConnectionHttpClient.builder().socketTimeout(Duration.ofSeconds(timeout)))
                    .region(Region.of(region))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    protected byte[] invokeExternal(byte[] threadCall) {
        debug(this.printPrefix() + "Calling AWS Lambda.");

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
            debug(this.printPrefix() + "AWS call completed.");
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
