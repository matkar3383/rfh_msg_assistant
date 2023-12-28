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
    private String lastFileContent;
    private File lastProcessedFile;
    private volatile boolean isRunning = true;

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
     */
    public void continuouslyScanServerLocation() {
        while (isRunning) {
            File directory = new File(directoryPath);

            if (!checkDirectory(directory)) {
                continue;
            }

            File oldestFile = findOldestFile(directory);

            if (oldestFile != null) {
                processOldestFile(oldestFile);
            }

            sleepForInterval();
        }
    }

    private boolean checkDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("The specified directory does not exist: " + directory.getAbsolutePath());
            return false;
        }
        return true;
    }

    private File findOldestFile(File directory) {
        File[] files = directory.listFiles();

        if (files == null || files.length == 0) {
            System.err.println("No files found in the specified directory: " + directory.getAbsolutePath());
            return null;
        }

        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        return files[0];
    }

    private void processOldestFile(File oldestFile) {
        try {
            String fileContent = readFileContent(oldestFile);

            if (!fileContent.equals(lastFileContent)) {
                logSuccessfulFileLoading(oldestFile.getName());
                lastFileContent = fileContent;
                lastProcessedFile = oldestFile; // Set the last processed file
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error while reading the file: " + e.getMessage());
        }
    }

    /**
     * Gets the last processed file.
     *
     * @return The last processed file.
     */
    public File getLastProcessedFile() {
        return lastProcessedFile;
    }

    private void logSuccessfulFileLoading(String fileName) {
        loggerManager.logInfo("Plik \"" + fileName + "\" został poprawnie wczytany.");
    }

    private void sleepForInterval() {
        try {
            // Sleep for the defined interval before checking again
            Thread.sleep(SCAN_INTERVAL_MS);
        } catch (InterruptedException e) {
            System.err.println("Thread sleep interrupted: " + e.getMessage());
        }
    }

    /**
     * Reads the content of the file.
     *
     * @param file The file to read.
     * @return The content of the file as a String.
     * @throws FileNotFoundException If the file is not found.
     */
    private String readFileContent(File file) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(file)) {
            StringBuilder contentBuilder = new StringBuilder();

            while (scanner.hasNextLine()) {
                contentBuilder.append(scanner.nextLine()).append("\n");
            }

            return contentBuilder.toString();
        }
    }
}
