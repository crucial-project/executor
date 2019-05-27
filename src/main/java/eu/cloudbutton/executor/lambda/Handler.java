package eu.cloudbutton.executor.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import eu.cloudbutton.executor.Marshalling;

import java.util.concurrent.Callable;

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
            Callable callable = Marshalling.fromBytes(input);
            byte[] result = Marshalling.toBytes(callable.call());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}