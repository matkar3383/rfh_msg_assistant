package com.coba;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading properties from the configuration file.
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

    /**
     * Initializes properties by loading them from the configuration file.
     */
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

    /**
     * Gets the loaded properties.
     *
     * @return The loaded properties.
     */
    public static Properties getProperties() {
        return properties;
    }

    /**
     * Gets the value for a specified key from the loaded properties.
     *
     * @param key The key for which to get the value.
     * @return The value for the specified key.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
