package com.coba;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileReader {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide the path to the properties file as a command line argument.");
            return;
        }

        String propertiesFilePath = args[0];
        Properties properties = new Properties();

        try (InputStream input = FileReader.class.getClassLoader().getResourceAsStream(propertiesFilePath)) {
            if (input == null) {
                System.out.println("Sorry, unable to find " + propertiesFilePath);
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Teraz możesz korzystać z wartości wczytanych z pliku properties
        String filePath = properties.getProperty("file.path");
        System.out.println("File Path: " + filePath);
    }
}
