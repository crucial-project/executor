package eu.cloudbutton.executor;

import eu.cloudbutton.executor.lambda.AWSLambdaExecutorService;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.*;

public class SShell {

    public static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static void main(String[] args) {

        Properties properties = System.getProperties();
        try (InputStream is = SShell.class.getClassLoader().getResourceAsStream(Config.CONFIG_FILE)) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService service = new AWSLambdaExecutorService(properties);

        StringBuilder builder = new StringBuilder();
        for (String str: args) {
            builder.append(str);
            builder.append(" ");
        }
        String command = builder.toString();

        Future<String> future = service.submit((Serializable & Callable<String>)()-> {
            String ret = "";
            try {
                ProcessBuilder b = new ProcessBuilder("/bin/sh", "-c", command);
                Process p  = b.start();
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                StringBuilder sbuilder = new StringBuilder();
                java.util.Scanner s = new java.util.Scanner(stdInput).useDelimiter("\\A");
                if (s.hasNext()) {
                    sbuilder.append(s.next());
                }
                s = new java.util.Scanner(stdError).useDelimiter("\\A");
                if (s.hasNext()) {
                    sbuilder.append(s.next());
                }
                p.waitFor();
                ret = sbuilder.toString();
            } catch (IOException e) {
                ret = e.getMessage();
            } catch (InterruptedException e) {
                ret = e.getMessage();
            }
            return ret;
        });

        try {
            String ret = future.get();
            System.out.println(ret);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

}
