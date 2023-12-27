package com.coba;

import java.io.IOException;

/**
 * Main application class for handling file scanning and message preparation for MQ.
 */
public final class App {

    private App() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }

    /**
     * Main method to run the application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        try {
            PropertiesLoader.getProperties();
            String fileScannerLogPath = PropertiesLoader.getProperty("fileScanner.log");
            String prepareMsgLogPath = PropertiesLoader.getProperty("prepareMsg.log");
            LoggerManager fileScannerLoggerManager = LoggerManager.getInstance(fileScannerLogPath);
            LoggerManager prepareMsgLoggerManager = LoggerManager.getInstance(prepareMsgLogPath);

            ServerFileScanner serverFileScanner = new ServerFileScanner(
                    PropertiesLoader.getProperty("server.directory"), fileScannerLoggerManager);

            String fileToSend = serverFileScanner.getFilePath();

            if (fileToSend != null) {
                MsgToMQ msgToMQ = new MsgToMQ(prepareMsgLoggerManager);
                msgToMQ.prepareAndSendToMQ(fileToSend);

                fileScannerLoggerManager.closeLogFile();
                prepareMsgLoggerManager.closeLogFile();
            } else {
                System.out.println("No files to send.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
