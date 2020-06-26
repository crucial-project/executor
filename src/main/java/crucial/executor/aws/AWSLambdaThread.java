package crucial.executor.aws;

import com.amazonaws.services.lambda.model.InvokeResult;
import crucial.executor.CloudThread;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

public class AWSLambdaThread extends CloudThread {

    private static AWSLambdaInvoker invoker;

    public AWSLambdaThread(Runnable target) {
        super(target);
        Properties properties = System.getProperties();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(crucial.executor.Config.CONFIG_FILE)) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        invoker = new AWSLambdaInvoker(properties);
        invoker.initClient();
    }

    @Override
    protected void invoke(byte[] threadCall) {
        System.out.println(this.printPrefix() + "Calling AWS Lambda.");
        InvokeResult result = invoker.invoke(threadCall);
        System.out.println(this.printPrefix() + "AWS call completed.");
        if (logs) {
            System.out.println(this.printPrefix() + "Showing Lambda Tail Logs.\n");
            assert result != null;
            System.out.println(new String(Base64.getDecoder().decode(result.getLogResult())));
        }
    }

    public static void closeInvoker(){
        invoker.stop();
    }
}
