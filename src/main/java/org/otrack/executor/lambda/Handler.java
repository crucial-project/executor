package org.otrack.executor.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static org.otrack.executor.Marshalling.*;

/**
 * General lambda handler for jobs. The different versions differ in how they load data.
 * <p>
 * Date: 15/11/2017
 *
 * @author Daniel
 */
public class Handler implements RequestHandler<byte[],byte[]>{

    @Override
    public byte[] handleRequest(byte[] input, Context context){
        try {
            Callable callable = fromBytes(input);
            byte[] result = toBytes(callable.call());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}