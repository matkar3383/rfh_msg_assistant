package com.coba;

import java.io.IOException;

/**
 * Main application class.
 */
public final class App {

    private App() {
        // Utility class should not have public or default constructor
        throw new AssertionError();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
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
            try {
                // Prepare and send message to MQ
                msgToMQ.prepareAndSendToMQ(file);

                // Log success message
                loggerManager.logInfo("Successfully processed and sent file to MQ: "
                        + serverFileScanner.getFileName());
            } catch (IOException e) {
                // Log error if an issue occurs during MQ message preparation or sending
                loggerManager.logError("Error processing or sending file to MQ: " + e.getMessage());
            }
        });

        // Shut down gracefully when the application is interrupted
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            serverFileScanner.stop();
            loggerManager.logInfo("Application shut down gracefully.");
        }));
    }
}
