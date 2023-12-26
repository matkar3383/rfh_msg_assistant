package com.coba;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading properties from the application.properties file.
 */
public final class PropertiesLoader {

    private static final String PROPERTIES_FILE_NAME = "application.properties";
    private static Properties properties;

    private PropertiesLoader() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Loads properties from the application.properties file.
     */
    public static void loadProperties() {
        properties = new Properties();
        try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME)) {
            if (input == null) {
                System.err.println("Sorry, unable to find " + PROPERTIES_FILE_NAME);
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Error loading " + PROPERTIES_FILE_NAME + ": " + e.getMessage());
        }
    }

    /**
     * Gets the value of a property.
     * @param key The key of the property.
     * @return The value of the property.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
