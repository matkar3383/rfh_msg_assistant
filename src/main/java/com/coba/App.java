package com.coba;

import java.io.File;
import java.io.IOException;

/**
 * Klasa uruchomieniowa aplikacji.
 * Ładuje właściwości, inicjalizuje i uruchamia ServerFileScanner oraz przekazuje pliki do MsgToMQ.
 */
public final class App {

    private App() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Metoda główna do uruchamiania aplikacji.
     *
     * @param args Argumenty wiersza poleceń.
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        // Ładuje właściwości aplikacji
        if (!PropertiesLoader.loadProperties()) {
            System.err.println("Nie udało się załadować właściwości aplikacji. Zamykanie...");
            System.exit(1);
        }

        // Pobiera ścieżkę katalogu z właściwości aplikacji
        String directoryPath = PropertiesLoader.getProperty("directory.path");

        // Inicjalizuje i uruchamia ServerFileScanner z instancją LogManager
        ServerFileScanner fileScanner = new ServerFileScanner(directoryPath, LogManager.getInstance(),
                new MsgToMQ(LogManager.getInstance()));
        fileScanner.continuouslyScanServerLocation();

        // Pobiera ostatnio przetworzony plik z ServerFileScanner
        File lastProcessedFile = fileScanner.getLastProcessedFile();

        // Sprawdza, czy ostatnio przetworzony plik nie jest null
        if (lastProcessedFile != null) {
            // Inicjalizuje instancję MsgToMQ z LogManager
            MsgToMQ msgToMQ = new MsgToMQ(LogManager.getInstance());

            // Wywołuje metodę prepareAndSendToMQ z ostatnio przetworzonym plikiem
            msgToMQ.prepareAndSendToMQ(lastProcessedFile.getAbsolutePath());
        }
    }
}
