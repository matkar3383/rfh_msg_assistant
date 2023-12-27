package com.coba;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logger manager class to configure and manage application logging.
 */
public final class LoggerManager {

    private static final String LOGS_DIRECTORY = "/logs";

    private static final Logger LOGGER = Logger.getLogger(LoggerManager.class.getName());

    private static final LoggerManager INSTANCE = new LoggerManager();

    private LoggerManager() {
        // Prevent instantiation
        if (INSTANCE != null) {
            throw new AssertionError("Use getInstance() method to get the single instance of this class.");
        }
    }

    /**
     * Gets the singleton instance of the LoggerManager.
     *
     * @return The LoggerManager instance.
     */
    public static LoggerManager getInstance() {
        return INSTANCE;
    }

    /**
     * Configures the logger and creates log files.
     *
     * @param logFileName The name of the log file.
     */
    public void configureLogger(String logFileName) {
        try {
            File logsDirectory = new File(LOGS_DIRECTORY);
            if (!logsDirectory.exists()) {
                if (logsDirectory.mkdirs()) {
                    logInfo("Logs directory created: " + LOGS_DIRECTORY);
                } else {
                    logError("Failed to create logs directory: " + LOGS_DIRECTORY);
                }
            }

            FileHandler fileHandler = new FileHandler(LOGS_DIRECTORY + File.separator + logFileName);
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            logError("Error configuring logger: " + e.getMessage());
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
}
