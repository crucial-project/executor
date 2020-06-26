package crucial.executor;

import com.google.gson.Gson;


public class Json {

    private static Gson gson = new Gson();

    public static String toJson(byte[] input) {
        return gson.toJson(input);
    }

    public static byte[] fromJson(String input) {
        return gson.fromJson(input, byte[].class);
    }

}
