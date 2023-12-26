package com.coba;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * This class scans a server location for a file and reads its content into a String.
 * It continuously checks for a file at the specified location and logs successful file loading.
 */
public class ServerFileScanner {

    private static final int SCAN_INTERVAL_MS = 1000;
    private final LoggerManager loggerManager;
    private final String filePath;
    private String lastFileContent;
    private volatile boolean isRunning = true;

    /**
     * Constructor to initialize the ServerFileScanner with the provided file path and LoggerManager instance.
     * @param filePath The path to the file on the server.
     * @param loggerManager The LoggerManager instance.
     */
    public ServerFileScanner(String filePath, LoggerManager loggerManager) {
        this.filePath = filePath;
        this.loggerManager = loggerManager;
    }

    /**
     * Stops the server file scanner.
     */
    public void stop() {
        isRunning = false;
    }

    /**
     * Continuously scans the server location for the specified file and reads its content.
     */
    public void continuouslyScanServerLocation() {
        while (isRunning) {
            File file = new File(filePath);

            // Check if the file exists
            if (file.exists()) {
                try {
                    // Read the content of the file into a String
                    Scanner scanner = new Scanner(file);
                    StringBuilder contentBuilder = new StringBuilder();

                    while (scanner.hasNextLine()) {
                        contentBuilder.append(scanner.nextLine()).append("\n");
                    }

                    scanner.close();
                    String fileContent = contentBuilder.toString();

                    // Check if the file content has changed
                    if (!fileContent.equals(lastFileContent)) {
                        // Log successful file loading
                        loggerManager.logInfo("Plik \"" + file.getName() + "\" został poprawnie wczytany.");

                        lastFileContent = fileContent;
                    }
                } catch (FileNotFoundException e) {
                    System.err.println("Error while reading the file: " + e.getMessage());
                }
            } else {
                System.err.println("The file at the specified path does not exist.");
            }

            try {
                // Sleep for the defined interval before checking again
                Thread.sleep(SCAN_INTERVAL_MS);
            } catch (InterruptedException e) {
                System.err.println("Thread sleep interrupted: " + e.getMessage());
            }
        }
    }
}
