package com.coba;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.headers.MQRFH2;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Class for preparing and sending messages to MQ.
 */
public class MsgToMQ {

    private static final int BUFFER_SIZE = 64;
    private final LogManager loggerManager;

    /*
     * Constructor for MsgToMQ.
     *
     * @param loggerManager The logger manager.
     */
    public MsgToMQ(LogManager loggerManager) {
        this.loggerManager = loggerManager;
    }

    /**
     * Prepare and send message to MQ.
     *
     * @param file The file to be sent as a message.
     * @throws java.io.IOException
     */
    public void prepareAndSendToMQ(String file) throws IOException {
        try {
            MQQueue queue = null;
            MQQueueManager queueManager = null;
            MQMessage msgForSending = new MQMessage();
            MQPutMessageOptions putMessageOptions = new MQPutMessageOptions();
            int bytesRead;
            int bytesTotalSize = 0;
            byte[] buf1 = new byte[BUFFER_SIZE];

            try (FileInputStream inputStream = new FileInputStream(file)) {
                loggerManager.logInfo("Connecting to Queue Manager: " + PropertiesLoader.getProperty("mq.qmgr"));
                queueManager = new MQQueueManager(PropertiesLoader.getProperty("mq.qmgr"), getMQProperties());
                loggerManager.logInfo("Opening the queue for writing: " + PropertiesLoader.getProperty("mq.queueName"));
                queue = queueManager.accessQueue(PropertiesLoader.getProperty("mq.queueName"), CMQC.MQOO_BROWSE);

                // Prepare and write RFH2 header
                MQRFH2 mqrfh2 = new MQRFH2();
                mqrfh2.setEncoding(Integer.parseInt(PropertiesLoader.getProperty("mq.encoding")));
                mqrfh2.setCodedCharSetId(Integer.parseInt(PropertiesLoader.getProperty("mq.codedCharSetId")));
                mqrfh2.setFormat(CMQC.MQFMT_RF_HEADER_2);
                mqrfh2.setFlags(0);
                // ... (Configure RFH2 header fields as needed)
                mqrfh2.setFieldValue("IF_COBA", "OriginatorApplication",
                        PropertiesLoader.getProperty("ifcoba.originatorApplication"));
                mqrfh2.setFieldValue("IF_COBA", "UserReference", PropertiesLoader.getProperty("ifcoba.userReference"));
                mqrfh2.setFieldValue("IF_COBA", "Requestor", PropertiesLoader.getProperty("ifcoba.requestor"));
                mqrfh2.setFieldValue("IF_COBA", "Responder", PropertiesLoader.getProperty("ifcoba.responder"));
                mqrfh2.setFieldValue("IF_COBA", "Service", PropertiesLoader.getProperty("ifcoba.service"));
                mqrfh2.setFieldValue("IF_COBA", "RequestType", PropertiesLoader.getProperty("ifcoba.requestType"));
                mqrfh2.setFieldValue("IF_COBA", "Compression", PropertiesLoader.getProperty("ifcoba.compression"));
                mqrfh2.setFieldValue("IF_COBA", "FileName", PropertiesLoader.getProperty("ifcoba.fileName"));
                mqrfh2.write(msgForSending);

                // Log message before sending
                logCompleteMessage(msgForSending);
                // Write message payload
                while ((bytesRead = inputStream.read(buf1)) != -1) {
                    msgForSending.write(buf1, 0, bytesRead);
                    bytesTotalSize += bytesRead;
                }

                loggerManager.logInfo("Finished writing " + bytesTotalSize + " bytes into message!");
                loggerManager.logInfo("Wrote message to the queue successfully: " + file);

                // Set MQMD values
                msgForSending.messageId = CMQC.MQMI_NONE;
                msgForSending.correlationId = CMQC.MQCI_NONE;
                msgForSending.messageType = CMQC.MQMT_DATAGRAM;
                msgForSending.putApplicationName = CMQC.APPNAME_PROPERTY;

                // Set the format to MQRFH2
                msgForSending.format = CMQC.MQFMT_RF_HEADER_2;
                // Put the message on the queue
                queue.put(msgForSending, putMessageOptions);

                loggerManager.logInfo("Successfully sent message to MQ: " + file);

            } finally {
                if (queue != null) {
                    queue.close();
                }
                if (queueManager != null) {
                    queueManager.disconnect();
                }
            }
        } catch (MQException mqe) {
            loggerManager.logError("MQException while preparing or sending message to MQ: " + mqe.getMessage());
        } catch (IOException ioe) {
            loggerManager.logError("IOException while preparing or sending message to MQ: " + ioe.getMessage());
        }
    }

    // Helper method to get MQ properties
    private Hashtable<String, Object> getMQProperties() {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(CMQC.HOST_NAME_PROPERTY, PropertiesLoader.getProperty("mq.host"));
        properties.put(CMQC.PORT_PROPERTY, Integer.valueOf(PropertiesLoader.getProperty("mq.port")));
        properties.put(CMQC.CHANNEL_PROPERTY, PropertiesLoader.getProperty("mq.channel"));
        properties.put(CMQC.USER_ID_PROPERTY, PropertiesLoader.getProperty("mq.appUser"));
        properties.put(CMQC.PASSWORD_PROPERTY, PropertiesLoader.getProperty("mq.appPassword"));
        return properties;
    }

    // Metoda do logowania całej wiadomości
    private void logCompleteMessage(MQMessage message) throws IOException, MQException {
        // Logowanie nagłówka
        StringBuilder headerLog = new StringBuilder("Message Header:\n");
        headerLog.append("Format: ").append(message.format).append("\n");
        headerLog.append("MessageId: ").append(message.messageId).append("\n");
        headerLog.append("CorrelationId: ").append(message.correlationId).append("\n");
        // ... (Dodaj logowanie innych pól nagłówka, jeśli to konieczne)

        // Logowanie właściwości (properties)
        StringBuilder propertiesLog = new StringBuilder("Message Properties:\n");
        String[] knownPropertyNames = {"property1", "property2", /* ... and other known property names ... */};

        for (String key : knownPropertyNames) {
            try {
                Object propertyValue = message.getObjectProperty(key);
                propertiesLog.append(key).append(": ").append(propertyValue).append("\n");
            } catch (MQException e) {
                // Obsługa wyjątku, jeśli coś pójdzie nie tak
                loggerManager.logError("Error getting property " + key + ": " + e.getMessage());
            }
        }

        // Logowanie zawartości
        StringBuilder contentLog = new StringBuilder("Message Content:\n");
        byte[] contentBytes = new byte[message.getMessageLength()];
        message.readFully(contentBytes);
        // Jeśli wiadomość zawiera tekst, użyj odpowiedniego kodowania
        String content = new String(contentBytes, message.characterSet);
        contentLog.append(content);

        // Sklejenie wszystkich logów
        String completeLog = headerLog.toString() + propertiesLog.toString() + contentLog.toString();

        // Logowanie do pliku
        LogManager.getInstance().logToFile(completeLog);
    }
}
