package crucial.executor;

import java.io.IOException;
import java.util.concurrent.Callable;

import static crucial.executor.ByteMarshaller.*;

public class CloudThreadHandler {

    protected byte[] handle(byte[] input) {
        Object result = null;
        try {
            crucial.executor.ThreadCall call = fromBytes(input);
            System.out.println(call.getThreadName() + " loaded.");
            Callable c = call.getTarget();
            result = c.call();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            result = e;
        }
        try {
            return toBytes(result);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
