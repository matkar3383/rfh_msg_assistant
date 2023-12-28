package com.coba;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Utility class for managing logging configuration.
 */
public final class LogManager {

    private static final Logger LOGGER = Logger.getLogger(LogManager.class.getName());
    private static LogManager instance;

    private final String logFilePath;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private LogManager() {
        this.logFilePath = getLogFilePath();
        configureLogger(logFilePath);
    }

    /**
     * Returns the singleton instance of LogManager.
     *
     * @return The LogManager instance.
     */
    public static LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    /**
     * Configures the logger based on the provided log file path.
     *
     * @param providedLogFilePath The path to the log file.
     */
    private void configureLogger(String providedLogFilePath) {
        try {
            // Check if the log file already exists
            File logFile = new File(providedLogFilePath);
            // Log the paths for debugging
            LOGGER.log(Level.INFO, "Provided log file path: {0}", providedLogFilePath);
            LOGGER.log(Level.INFO, "Parent directory: {0}", logFile.getParent());
            if (!logFile.exists()) {
                // Create the log file and its parent directories
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }
            // Create a file handler that writes log records to the log file
            FileHandler fileHandler = new FileHandler(logFile.getPath(), true); // 'true' appends to the file
            fileHandler.setFormatter(new SimpleFormatter());
            // Add the file handler to the logger
            LOGGER.addHandler(fileHandler);
            // Flush the file handler to ensure logs are written immediately
            fileHandler.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error configuring logger: {0}", e.getMessage());
        }
    }

    /**
     * Logs an informational message.
     *
     * @param message The message to log.
     */
    public void logInfo(String message) {
        LOGGER.log(Level.INFO, message);
    }

    /**
     * Logs an error message.
     *
     * @param message The error message to log.
     */
    public void logError(String message) {
        LOGGER.log(Level.SEVERE, message);
    }

    /**
     * Logs a message to the file directly without going through the logger.
     *
     * @param message The message to log.
     */
    public void logToFile(String message) {
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(message);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error writing to log file: {0}", e.getMessage());
        }
    }

    /**
     * Gets the log file path from the application properties.
     *
     * @return The log file path.
     */
    private String getLogFilePath() {
        // Assuming application.properties is in the classpath and contains the property
        // "fileScanner.log"
        return PropertiesLoader.getProperty("fileScanner.log");
    }
}
