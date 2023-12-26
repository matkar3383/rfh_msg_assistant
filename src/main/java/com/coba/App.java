package com.coba;

/**
 * Hello world!
 */
public final class App {

    private App() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        // Load application properties
        PropertiesLoader.loadProperties();

        // Get file path from application properties
        String filePath = PropertiesLoader.getProperty("file.path");

        // Create and run ServerFileScanner with LoggerManager instance
        ServerFileScanner fileScanner = new ServerFileScanner(filePath, LoggerManager.getInstance());
        fileScanner.continuouslyScanServerLocation();
    }
}
