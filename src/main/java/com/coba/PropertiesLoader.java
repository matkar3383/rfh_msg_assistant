package com.coba;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Klasa narzędziowa do wczytywania właściwości z pliku.
 */
public final class PropertiesLoader {
    @SuppressWarnings("checkstyle:ConstantName")
    private static final Properties properties = new Properties();

    private PropertiesLoader() {
        // Prywatny konstruktor, aby zapobiec instancjonowaniu
    }

    /**
     * Wczytuje właściwości z pliku.
     *
     * @return true, jeśli właściwości zostały pomyślnie wczytane, false w przeciwnym razie.
     */
    public static boolean loadProperties() {
        try (FileInputStream input = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(input);
            return true; // Zwróć true, jeśli właściwości zostały pomyślnie wczytane
        } catch (IOException e) {
            System.err.println("Błąd podczas wczytywania właściwości: " + e.getMessage());
            return false; // Zwróć false, jeśli wystąpi błąd podczas wczytywania
        }
    }

    /**
     * Pobiera wartość właściwości na podstawie klucza.
     *
     * @param key Klucz właściwości.
     * @return Wartość właściwości lub null, jeśli klucz nie został znaleziony.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
