package org.mericoztiryaki.domain.util;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Properties;

@Log4j2
public class Environment {

    private static final String ENV_DEV = "dev";

    private static final Properties properties;

    static {
        // Create properties object
        properties = new Properties();
        try {
            properties.load(Environment.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            log.fatal("Properties file can't opened, default values will be used.");
        }
    }

    public static final String PRICE_API_HOST = readEnvVariable("PRICE_API_HOST", "http://127.0.0.1:8000");

    public static final String PRICE_CACHE_PATH = readEnvVariable("PRICE_CACHE_PATH", System.getProperty("java.io.tmpdir") + "pvis-price-cache.bin");

    private static String readEnvVariable(String name, String defaultValue) {
        String val = properties.getProperty(name);
        if (val != null) {
            return val;
        }
        return defaultValue;
    }

}
