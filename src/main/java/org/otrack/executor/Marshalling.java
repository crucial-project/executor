package org.otrack.executor;

import com.google.gson.Gson;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Base64;

public class Marshalling {

    public static byte[] toBytes(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        return baos.toByteArray();
    }

    public static <T> T fromBytes(byte[] input) throws IOException, ClassNotFoundException {
        return (T) new ObjectInputStream(new ByteArrayInputStream(input)).readObject();
    }

}
