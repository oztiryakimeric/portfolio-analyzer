package org.mericoztiryaki.domain.util;

public class Environment {

    public static final String PRICE_API_HOST = readEnvVariable("PRICE_API_HOST", "http://127.0.0.1:8000");

    private static String readEnvVariable(String name, String defaultValue) {
        String val = System.getenv(name);
        if (val != null) {
            return val;
        }
        return defaultValue;
    }
}
