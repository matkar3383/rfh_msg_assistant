package com.coba;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author MateOo
 */
public final class PropertiesLoader {

    private static final String PROPERTIES_FILE = "application.properties";
    private static Properties properties;

    static {
        initProperties();
    }

    private PropertiesLoader() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    private static void initProperties() {
        properties = new Properties();
        try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                System.out.println("Sorry, unable to find " + PROPERTIES_FILE);
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
