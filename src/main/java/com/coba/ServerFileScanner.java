package com.coba;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

/**
 * This class scans a server location for a file and reads its content into a
 * String.
 * It continuously checks for a file at the specified location and logs
 * successful file loading.
 */
public class ServerFileScanner {

    private static final int SCAN_INTERVAL_MS = 1000;
    private final LoggerManager loggerManager;
    private final String directoryPath;
    private String lastFilePath;
    private String lastFileName;
    private volatile boolean isRunning = true;

    /**
     * Constructs a ServerFileScanner.
     *
     * @param directoryPath The path to the server directory.
     * @param loggerManager The logger manager.
     */
    public ServerFileScanner(String directoryPath, LoggerManager loggerManager) {
        this.directoryPath = directoryPath;
        this.loggerManager = loggerManager;
    }

    /**
     * Stops the server file scanner.
     */
    public void stop() {
        isRunning = false;
    }

    /**
     * Continuously scans the server location for the specified file and reads its
     * content.
     *
     * @param fileProcessor The processor to handle the file when found.
     */
    public void continuouslyScanServerLocation(FileProcessor fileProcessor) {
        while (isRunning) {
            File directory = new File(directoryPath);

            if (!checkDirectory(directory)) {
                continue;
            }

            File oldestFile = findOldestFile(directory);

            if (oldestFile != null) {
                processOldestFile(oldestFile, fileProcessor);
            }

            sleepForInterval();
        }
    }

    /**
     * Gets the file path of the last processed file.
     *
     * @return The file path.
     */
    public String getFilePath() {
        return lastFilePath;
    }

    /**
     * Gets the name of the last processed file.
     *
     * @return The file name.
     */
    public String getFileName() {
        return lastFileName;
    }

    private boolean checkDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            loggerManager.logInfo("The specified directory does not exist: " + directory.getAbsolutePath());
            return false;
        }
        return true;
    }

    private File findOldestFile(File directory) {
        File[] files = directory.listFiles();

        if (files == null || files.length == 0) {
            loggerManager.logInfo("No files found in the specified directory: " + directory.getAbsolutePath());
            return null;
        }

        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        return files[0];
    }

    private void processOldestFile(File file, FileProcessor fileProcessor) {
        lastFilePath = file.getAbsolutePath();
        lastFileName = file.getName();

        try (Scanner scanner = new Scanner(file)) {
            StringBuilder contentBuilder = new StringBuilder();

            while (scanner.hasNext()) {
                contentBuilder.append(scanner.nextLine());
            }

            loggerManager.logInfo("Successfully loaded file: " + lastFileName);
        } catch (FileNotFoundException e) {
            loggerManager.logError("File not found: " + lastFilePath);
        }
    }

    private void sleepForInterval() {
        try {
            Thread.sleep(SCAN_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Interface for processing a file.
     */
    public interface FileProcessor {
        /**
         * Process the file.
         *
         * @param file The file to process.
         */
        void processFile(File file);
    }
}
