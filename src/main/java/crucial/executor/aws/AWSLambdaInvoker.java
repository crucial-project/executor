package crucial.executor.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import crucial.executor.Json;
import crucial.executor.Config;

import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * Invoke calls at AWS Lambda.
 *
 * @author Daniel
 */
class AWSLambdaInvoker {
    private final AWSLambda lambdaClient;
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
        lambdaClient = AWSLambdaClientBuilder.standard()
                .withRegion(region)
                .withClientConfiguration(
                        new ClientConfiguration()
                                .withMaxConnections(1000)
                                .withSocketTimeout(600_000)
                                .withConnectionTimeout(10 * 1000)
                                .withMaxErrorRetry(3)
                ).build();
    }

    public void initClient() {
        GetFunctionRequest gf = new GetFunctionRequest();
        gf.setFunctionName(arn);
        lambdaClient.getFunction(gf);
    }

    /**
     * Synchronous Lambda invocation. With tail logs.
     *
     * @param payload Input for the Lambda
     * @return InvokeResult of the call.
     */
    InvokeResult invoke(byte[] payload) {
        return invoke(payload, true);
    }

    /**
     * Synchronous Lambda invocation.
     *
     * @param payload  Input for the Lambda
     * @param tailLogs Request tail logs?
     * @return InvokeResult of the call.
     */
    InvokeResult invoke(byte[] payload, boolean tailLogs) {
        InvokeRequest req = new InvokeRequest();
        req.setFunctionName(arn);
        if (async) {
            req.setInvocationType(InvocationType.Event);
        } else {
            req.setInvocationType(InvocationType.RequestResponse);
        }
        req.setPayload(ByteBuffer.wrap(Json.toJson(payload).getBytes()));
        if (tailLogs) req.setLogType(LogType.Tail);
        return lambdaClient.invoke(req);
    }

    void stop() {
        lambdaClient.shutdown();
    }

}
