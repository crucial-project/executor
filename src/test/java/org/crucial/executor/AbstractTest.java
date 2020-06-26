package org.crucial.executor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractTest {

    protected Properties properties;

    public AbstractTest(){
        properties = System.getProperties();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(Config.CONFIG_FILE)) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
