package org.crucial.executor;

import org.testng.annotations.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;

import static org.testng.AssertJUnit.assertTrue;

@Test
public class Marshalling {

    public void threadCall() {
        try {
            ThreadCall threadCall = new ThreadCall("test");
            threadCall.setTarget((Callable<Void> & Serializable) () -> {
                return null;
            });
            ByteMarshaller.fromBytes(ByteMarshaller.toBytes(threadCall));
        }catch (IOException | ClassNotFoundException e) {
            assertTrue(false);
        }
    }
}
