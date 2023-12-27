package com.coba;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Main application class.
 */
public final class App {

    private static final int SCAN_INTERVAL_MS = 1000;
    private static final LoggerManager LOGGER_MANAGER = LoggerManager.getInstance();
    private static final String DIRECTORY_PATH = PropertiesLoader.getProperty("directory.path");
    private static final MsgToMQ MSG_TO_MQ = new MsgToMQ(LOGGER_MANAGER);

    private App() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        while (true) {
            ServerFileScanner fileScanner = new ServerFileScanner(DIRECTORY_PATH, LOGGER_MANAGER);
            File file = fileScanner.findOldestFile();

            if (file != null) {
                String messageContent = readFileContent(file);
                logMessageContent(messageContent);

                // Add RFH2 header
                messageContent = addRFH2Header(messageContent);

                logPreparedMessage(messageContent);
                MSG_TO_MQ.prepareAndSendToMQ(messageContent);
                logSuccessfulFileSending(file.getName());
            }

            sleepForInterval();
        }
    }

    private static void logMessageContent(String messageContent) {
        LOGGER_MANAGER.logInfo("Read message content:\n" + messageContent);
    }

    private static void logPreparedMessage(String messageContent) {
        LOGGER_MANAGER.logInfo("Prepared message for MQ:\n" + messageContent);
    }

    private static void logSuccessfulFileSending(String fileName) {
        LOGGER_MANAGER.logInfo("File \"" + fileName + "\" was successfully sent.");
    }

    private static void sleepForInterval() {
        try {
            Thread.sleep(SCAN_INTERVAL_MS);
        } catch (InterruptedException e) {
            LOGGER_MANAGER.logInfo("Thread sleep interrupted: " + e.getMessage());
        }
    }

    private static String readFileContent(File file) {
        try (Scanner scanner = new Scanner(file)) {
            StringBuilder contentBuilder = new StringBuilder();

            while (scanner.hasNextLine()) {
                contentBuilder.append(scanner.nextLine()).append("\n");
            }

            return contentBuilder.toString();
        } catch (FileNotFoundException e) {
            LOGGER_MANAGER.logInfo("Error while reading the file: " + e.getMessage());
            return "";
        }
    }

    private static String addRFH2Header(String messageContent) {
        // Implementation of adding RFH2 header to the message content
        return messageContent;
    }
}
