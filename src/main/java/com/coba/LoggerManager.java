package com.coba;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Utility class for managing logging configuration.
 */
public final class LoggerManager {

    private static final Logger LOGGER = Logger.getLogger(LoggerManager.class.getName());
    private static LoggerManager instance;

    private LoggerManager() {
        configureLogger(getLogFilePath());
    }

    /**
     * Gets a singleton instance of LoggerManager.
     * @return The singleton instance of LoggerManager.
     */
    public static LoggerManager getInstance() {
        if (instance == null) {
            instance = new LoggerManager();
        }
        return instance;
    }

    /**
     * Configures logger settings.
     * @param logFilePath The path to the log file.
     */
    private void configureLogger(String logFilePath) {
        try {
            // Check if the log file already exists
            File logFile = new File(logFilePath);
            // Log the paths for debugging
            LOGGER.info("Provided log file path: " + logFilePath);
            LOGGER.info("Parent directory: " + logFile.getParent());
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
            LOGGER.severe("Error configuring logger: " + e.getMessage());
        }
    }

    /**
    * Logs an info message.
    * @param message The message to log.
    */
    public void logInfo(String message) {
        LOGGER.info(message);
    }

    /**
     * Get the path for the log file from application.properties.
     * @return The path to the log file.
     */
    private String getLogFilePath() {
        // Assuming application.properties is in the classpath and contains the property "fileScanner.log"
        return PropertiesLoader.getProperty("fileScanner.log");
    }
}
