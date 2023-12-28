package com.coba;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Klasa do skanowania lokalizacji serwera w poszukiwaniu plików.
 * Odczytuje zawartość pliku i przekazuje go do instancji klasy MsgToMQ.
 * Loguje informacje o wczytanych plikach.
 */
public class ServerFileScanner {

    private static final int SCAN_INTERVAL_MS = 1000;
    private final LogManager loggerManager;
    private final String directoryPath;
    private String lastFileContent;
    private File lastProcessedFile;
    private volatile boolean isRunning = true;
    private MsgToMQ msgToMQ;  // Dodana deklaracja pola msgToMQ

    /**
     * Konstruktor klasy ServerFileScanner.
     *
     * @param directoryPath  Ścieżka do katalogu serwera.
     * @param loggerManager  Instancja LogManager do logowania.
     * @param msgToMQ        Instancja MsgToMQ do przekazywania plików.
     */
    public ServerFileScanner(String directoryPath, LogManager loggerManager, MsgToMQ msgToMQ) {
        this.directoryPath = directoryPath;
        this.loggerManager = loggerManager;
        this.msgToMQ = msgToMQ;  // Dodane przypisanie msgToMQ
    }

    /**
     * Metoda zatrzymująca skanowanie serwera.
     */
    public void stop() {
        isRunning = false;
    }

    /**
     * Metoda ciągłego skanowania lokalizacji serwera w poszukiwaniu plików.
     * @throws java.io.IOException
     */
    public void continuouslyScanServerLocation() throws IOException {
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
            loggerManager.logError("Podany katalog nie istnieje: " + directory.getAbsolutePath());
            return false;
        }
        return true;
    }

    private File findOldestFile(File directory) {
        File[] files = directory.listFiles();

        if (files == null || files.length == 0) {
            loggerManager.logError("Brak plików w podanym katalogu: " + directory.getAbsolutePath());
            return null;
        }

        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        return files[0];
    }

    private void processOldestFile(File oldestFile) throws IOException {
        try {
            String fileContent = readFileContent(oldestFile);

            if (!fileContent.equals(lastFileContent)) {
                logSuccessfulFileLoading(oldestFile.getName());
                lastFileContent = fileContent;
                lastProcessedFile = oldestFile; // Ustawia ostatnio przetworzony plik

                // Dodane wywołanie, aby przekazać plik do MsgToMQ
                msgToMQ.prepareAndSendToMQ(lastProcessedFile.getAbsolutePath());
            }
        } catch (FileNotFoundException e) {
            loggerManager.logError("Błąd podczas czytania pliku: " + e.getMessage());
        }
    }

    /**
     * Pobiera ostatnio przetworzony plik.
     *
     * @return Ostatnio przetworzony plik.
     */
    public File getLastProcessedFile() {
        return lastProcessedFile;
    }

    private void logSuccessfulFileLoading(String fileName) {
        loggerManager.logInfo("Plik \"" + fileName + "\" został poprawnie wczytany.");
    }

    private void sleepForInterval() {
        try {
            // Czekaj przez zdefiniowany interwał przed kolejnym sprawdzeniem
            Thread.sleep(SCAN_INTERVAL_MS);
        } catch (InterruptedException e) {
            loggerManager.logError("Przerwane oczekiwanie wątku: " + e.getMessage());
        }
    }

    /**
     * Odczytuje zawartość pliku.
     *
     * @param file Plik do odczytu.
     * @return Zawartość pliku jako String.
     * @throws FileNotFoundException Jeśli plik nie zostanie znaleziony.
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
