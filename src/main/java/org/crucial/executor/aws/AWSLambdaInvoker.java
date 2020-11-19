package org.crucial.executor.aws;

import org.crucial.executor.Config;
import org.crucial.executor.Json;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

import java.util.Properties;

/**
 * Invoke calls at AWS Lambda.
 *
 * @author Daniel
 */
class AWSLambdaInvoker {
    private final LambdaClient lambdaClient;
    private final String region;
    private final String arn;
    private final boolean async;

    AWSLambdaInvoker(Properties properties) {
        this.region = properties.containsKey(Config.AWS_LAMBDA_REGION) ?
                properties.getProperty(Config.AWS_LAMBDA_REGION) : Config.AWS_LAMBDA_REGION_DEFAULT;
        this.arn = properties.containsKey(Config.AWS_LAMBDA_FUNCTION_ARN) ?
                properties.getProperty(Config.AWS_LAMBDA_FUNCTION_ARN) : Config.AWS_LAMBDA_FUNCTION_ARN_DEFAULT;
        this.async = Boolean.parseBoolean(properties.containsKey(Config.AWS_LAMBDA_FUNCTION_ASYNC) ?
                properties.getProperty(Config.AWS_LAMBDA_FUNCTION_ASYNC) : Config.AWS_LAMBDA_FUNCTION_ASYNC_DEFAULT);
        lambdaClient = LambdaClient.builder()
                .region(Region.of(region))
                .build();
    }

    public void initClient() {
        GetFunctionRequest gf = GetFunctionRequest.builder().functionName(arn).build();
        lambdaClient.getFunction(gf);
    }

    /**
     * Synchronous Lambda invocation. With tail logs.
     *
     * @param payload Input for the Lambda
     * @return InvokeResult of the call.
     */
    InvokeResponse invoke(byte[] payload) {
        return invoke(payload, true);
    }

    /**
     * Synchronous Lambda invocation.
     *
     * @param payload  Input for the Lambda
     * @param tailLogs Request tail logs?
     * @return InvokeResult of the call.
     */
    InvokeResponse invoke(byte[] payload, boolean tailLogs) {
        InvokeRequest.Builder builder = InvokeRequest.builder();
        builder.functionName(arn);
        if (async) {
            builder.invocationType(InvocationType.EVENT);
        } else {
            builder.invocationType(InvocationType.REQUEST_RESPONSE);
        }
        builder.payload(SdkBytes.fromByteArray(Json.toJson(payload).getBytes()));
        if (tailLogs) builder.logType(LogType.TAIL);
        return lambdaClient.invoke(builder.build());
    }

    void stop() {
        lambdaClient.close();
    }

}
