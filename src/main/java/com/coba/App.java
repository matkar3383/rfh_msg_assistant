package com.coba;

/**
 * Main application class.
 */
public final class App {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private App() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Main method of the application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        // Initialize LoggerManager (should only be done once)
        LoggerManager loggerManager = LoggerManager.getInstance();

        // Load application properties
        if (PropertiesLoader.getProperties() == null) {
            loggerManager.logError("Failed to load application properties. Exiting...");
            System.exit(1);
        }

        // Get directory path from application.properties
        String directoryPath = PropertiesLoader.getProperty("server.directory");

        // Initialize server file scanner
        ServerFileScanner serverFileScanner = new ServerFileScanner(directoryPath, loggerManager);

        // Initialize MQ message preparer
        MsgToMQ msgToMQ = new MsgToMQ(loggerManager);

        // Continuously scan the server location
        serverFileScanner.continuouslyScanServerLocation(file -> {
            // Prepare and send message to MQ
            msgToMQ.prepareAndSendToMQ(file);
            // Log success message
            loggerManager.logInfo("Successfully processed and sent file to MQ: " + file.getName());
        });

        // Shut down gracefully when the application is interrupted
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            serverFileScanner.stop();
            loggerManager.logInfo("Application shut down gracefully.");
        }));
    }
}
