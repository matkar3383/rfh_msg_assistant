package com.coba;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.headers.MQRFH2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;

public class PrepareMqMessage {

    public static void main(String[] args) throws MQException {
        // Załóżmy, że wcześniej odczytaliśmy parametry połączenia i dane z pliku XML
        String host = "your_host";
        int port = 1414;
        String channel = "your_channel";
        String user = "your_user";
        String password = "your_password";

        // Odczyt danych z pliku XML
        String xmlFilePath = "path/to/your/xml/file.xml";
        String xmlContent = readXmlFile(xmlFilePath);

        // Przygotowanie wiadomości
        MQQueueManager queueManager = prepareConnection(host, port, channel, user, password);
        MQQueue queue = prepareQueue(queueManager, "your_queue_name");

        // Tworzenie nagłówka IF_COBA
        MQRFH2 mqrfh2 = createIfCobaHeader();
        byte[] xmlBytes = xmlContent.getBytes(StandardCharsets.UTF_8);

        // Ustawienia MQMD
        MQMessage msgForSending = new MQMessage();
        msgForSending.messageId = CMQC.MQMI_NONE;
        msgForSending.correlationId = CMQC.MQCI_NONE;
        msgForSending.messageType = CMQC.MQMT_DATAGRAM;
        msgForSending.putApplicationName = CMQC.APPNAME_PROPERTY;
        msgForSending.format = CMQC.MQFMT_RF_HEADER_2;

        try {
            // Ustawienie nagłówka IF_COBA
            mqrfh2.write(msgForSending);
            // Zapisanie treści XML
            msgForSending.write(xmlBytes);
            // Wysłanie wiadomości do kolejki
            queue.put(msgForSending);
            System.out.println("Message sent successfully.");
        } catch (IOException | MQException e) {
            e.printStackTrace();
        } finally {
            // Zamknięcie połączenia
            try {
                queue.close();
                queueManager.disconnect();
            } catch (MQException e) {
                e.printStackTrace();
            }
        }
    }

    private static MQQueueManager prepareConnection(String host, int port, String channel, String user, String password) {
        Hashtable<String, Object> properties;
        properties = new Hashtable<>();
        properties.put(CMQC.HOST_NAME_PROPERTY, host);
        properties.put(CMQC.PORT_PROPERTY, port);
        properties.put(CMQC.CHANNEL_PROPERTY, channel);
        properties.put(CMQC.USER_ID_PROPERTY, user);
        properties.put(CMQC.PASSWORD_PROPERTY, password);

        try {
            return new MQQueueManager("QMGR", properties);
        } catch (MQException e) {
            throw new RuntimeException("Error establishing connection to the queue manager.", e);
        }
    }

    private static MQQueue prepareQueue(MQQueueManager queueManager, String queueName) {
        try {
            return queueManager.accessQueue(queueName, CMQC.MQOO_OUTPUT);
        } catch (MQException e) {
            throw new RuntimeException("Error accessing the queue.", e);
        }
    }

    private static MQRFH2 createIfCobaHeader() {
        MQRFH2 mqrfh2 = new MQRFH2();
        mqrfh2.setEncoding(CMQC.MQENC_NATIVE);
        mqrfh2.setCodedCharSetId(CMQC.MQCCSI_INHERIT);
        mqrfh2.setFormat(CMQC.MQFMT_NONE);
        mqrfh2.setFlags(0);
        mqrfh2.setNameValueCCSID(1208);

        // Ustawienia nagłówka IF_COBA
        mqrfh2.setFieldValue("IF_COBA", "Direction", "XXXX");
        mqrfh2.setFieldValue("IF_COBA", "OriginatorApplication", "XXXX");
        mqrfh2.setFieldValue("IF_COBA", "Owner", "XXXX");
        // ... Pozostałe ustawienia

        return mqrfh2;
    }

    private static String readXmlFile(String filePath) {
        try {
            return Files.readString(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Error reading XML file.", e);
        }
    }

    public static void processFileContent(String fileContent) {
    }
}
