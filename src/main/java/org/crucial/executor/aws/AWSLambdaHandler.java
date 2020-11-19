package org.crucial.executor.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.crucial.executor.CloudThreadHandler;

public class AWSLambdaHandler extends CloudThreadHandler
        implements RequestHandler<byte[], byte[]> {

    @Override
    public byte[] handleRequest(byte[] bytes, Context context) {
        return handle(bytes);
    }
}
