package eu.cloudbutton.executor;

import com.google.gson.Gson;

import java.io.IOException;

public class Json {

    private static Gson gson = new Gson();

    public static String toJson(byte [] input) throws IOException {
        return gson.toJson(input);
    }

    public static byte[] fromJson(String input) throws IOException, ClassNotFoundException {
        return gson.fromJson(input,byte[].class);
    }

}
