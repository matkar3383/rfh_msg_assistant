package com.coba;

/**
 * Hello world!
 */
public final class App {

    private App() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Main method to run the application.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        // Load application properties
        if (!PropertiesLoader.loadProperties()) {
            System.err.println("Failed to load application properties. Exiting...");
            System.exit(1);
        }

        // Get directory path from application properties
        String directoryPath = PropertiesLoader.getProperty("directory.path");

        // Create and run ServerFileScanner with LoggerManager instance
        ServerFileScanner fileScanner = new ServerFileScanner(directoryPath, LoggerManager.getInstance());
        fileScanner.continuouslyScanServerLocation();
    }
}
