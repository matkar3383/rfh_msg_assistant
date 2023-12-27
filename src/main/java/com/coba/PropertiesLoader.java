package com.coba;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading properties from a file.
 */
public final class PropertiesLoader {

    private static final String PROPERTIES_FILE = "src/main/resources/application.properties";
    private static final Properties PROPERTIES = loadProperties();

    private PropertiesLoader() {
        // private constructor to prevent instantiation
    }

    /**
     * Load properties from the file.
     *
     * @return The loaded properties.
     */
    static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new IOException("Sorry, unable to find " + PROPERTIES_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
        return properties;
    }

    /**
     * Get the value of a property.
     *
     * @param key The key of the property.
     * @return The value of the property.
     */
    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }
}
