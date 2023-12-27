package com.coba;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Utility class for managing logs using java.util.logging.
 */
public final class LoggerManager {

    private final Logger logger;

    /**
     * Private constructor to prevent instantiation from outside.
     *
     * @param logFilePath The path to the log file.
     * @throws IOException If an I/O error occurs.
     */
    private LoggerManager(String logFilePath) throws IOException {
        FileHandler fileHandler = new FileHandler(logFilePath, true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger = Logger.getLogger(LoggerManager.class.getName());
        logger.addHandler(fileHandler);
        logger.setLevel(Level.ALL); // Set the logging level to ALL
    }

    /**
     * Gets an instance of LoggerManager.
     *
     * @param logFilePath The path to the log file.
     * @return The LoggerManager instance.
     * @throws IOException If an I/O error occurs.
     */
    public static LoggerManager getInstance(String logFilePath) throws IOException {
        return new LoggerManager(logFilePath);
    }

    /**
     * Logs an informational message.
     *
     * @param message The message to log.
     */
    public void logInfo(String message) {
        logger.info(message);
    }

    /**
     * Logs an error message.
     *
     * @param message The error message to log.
     */
    public void logError(String message) {
        logger.severe(message);
    }

    /**
     * Closes the log file.
     */
    public void closeLogFile() {
        for (Handler handler : logger.getHandlers()) {
            handler.close();
        }
    }
}
