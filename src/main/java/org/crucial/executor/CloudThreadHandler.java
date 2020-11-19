package org.crucial.executor;

import software.amazon.awssdk.core.SdkBytes;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class CloudThreadHandler {

    protected byte[] handle(byte[] input) {
        Object result = null;
        try {
            ThreadCall call = ByteMarshaller.fromBytes(input);
            Callable c = call.getTarget();
            result = c.call();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            result = e;
        }
        try {
            byte[] ret = ByteMarshaller.toBytes(result);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
