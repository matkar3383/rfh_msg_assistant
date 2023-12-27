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

    private static final Logger FILE_SCANNER_LOGGER = Logger.getLogger("FileScannerLogger");
    private static final Logger PREPARE_MSG_LOGGER = Logger.getLogger("PrepareMsgLogger");
    private static LoggerManager instance;

    private LoggerManager() {
        configureLogger(FILE_SCANNER_LOGGER, getLogFilePath());
        configureLogger(PREPARE_MSG_LOGGER, getPrepareMsgLogFilePath());
    }

    /**
     * Gets a singleton instance of LoggerManager.
     *
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
     *
     * @param logger      The logger instance to configure.
     * @param logFilePath The path to the log file.
     */
    private void configureLogger(Logger logger, String logFilePath) {
        try {
            // Check if the log file already exists
            File logFile = new File(logFilePath);
            // Log the paths for debugging
            logger.info("Provided log file path: " + logFilePath);
            logger.info("Parent directory: " + logFile.getParent());
            if (!logFile.exists()) {
                // Create the log file and its parent directories
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }
            // Create a file handler that writes log records to the log file
            FileHandler fileHandler = new FileHandler(logFile.getPath(), true); // 'true' appends to the file
            fileHandler.setFormatter(new SimpleFormatter());
            // Add the file handler to the logger
            logger.addHandler(fileHandler);
            // Flush the file handler to ensure logs are written immediately
            fileHandler.flush();
        } catch (IOException e) {
            logger.severe("Error configuring logger: " + e.getMessage());
        }
    }

    /**
     * Logs an info message for FileScannerLogger.
     *
     * @param message The message to log.
     */
    public void logInfo(String message) {
        FILE_SCANNER_LOGGER.info(message);
    }

    /**
     * Logs an info message for PrepareMsgLogger.
     *
     * @param message The message to log.
     */
    public void logPrepareMsg(String message) {
        PREPARE_MSG_LOGGER.info(message);
    }

    /**
     * Get the path for the log file from application.properties for
     * FileScannerLogger.
     *
     * @return The path to the log file.
     */
    private String getLogFilePath() {
        // Assuming application.properties is in the classpath and contains the property
        // "fileScanner.log"
        return PropertiesLoader.getProperty("fileScanner.log");
    }

    /**
     * Get the path for the log file from application.properties for
     * PrepareMsgLogger.
     *
     * @return The path to the log file.
     */
    private String getPrepareMsgLogFilePath() {
        // Assuming application.properties is in the classpath and contains the property
        // "prepareMsg.log"
        return PropertiesLoader.getProperty("prepareMsg.log");
    }

    /**
     * Logs an error message for both loggers.
     *
     * @param message The error message to log.
     */
    public void logError(String message) {
        FILE_SCANNER_LOGGER.severe(message);
        PREPARE_MSG_LOGGER.severe(message);
    }
}
