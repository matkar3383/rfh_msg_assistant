package com.coba;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.headers.MQRFH2;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

/**
 * Class for preparing and sending messages to MQ.
 */
public class MsgToMQ {

    private static final Logger LOG = Logger.getLogger(MsgToMQ.class.getName());
    private final LoggerManager loggerManager;

    /**
     * Constructor for MsgToMQ.
     *
     * @param loggerManager The logger manager.
     */
    public MsgToMQ(LoggerManager loggerManager) {
        this.loggerManager = loggerManager;
    }

    /**
     * Prepares and sends the message to MQ.
     *
     * @param file             The file to read and send.
     * @param queueManagerName The name of the MQ queue manager.
     * @param outgoingQueue    The name of the outgoing MQ queue.
     * @param properties       The properties for connecting to MQ.
     * @throws MQException If there is an issue with MQ.
     * @throws IOException If there is an issue reading the file.
     */
    public void prepareAndSendToMQ(File file, String queueManagerName, String outgoingQueue,
            Hashtable<String, Object> properties) throws MQException, IOException {
        // Load application properties
        if (!PropertiesLoader.loadProperties("application.properties")) {
            loggerManager.logError("Failed to load application properties. Exiting...");
            System.exit(1);
        }

        // Get values from application.properties
        String host = PropertiesLoader.getProperty("mq.host");
        int port = Integer.parseInt(PropertiesLoader.getProperty("mq.port"));
        String channel = PropertiesLoader.getProperty("mq.channel");
        String appUser = PropertiesLoader.getProperty("mq.appUser");
        String appPassword = PropertiesLoader.getProperty("mq.appPassword");
        String qmgr = PropertiesLoader.getProperty("mq.qmgr");
        String queueName = PropertiesLoader.getProperty("mq.queueName");

        int encoding = Integer.parseInt(PropertiesLoader.getProperty("mq.encoding"));
        int codedCharSetId = Integer.parseInt(PropertiesLoader.getProperty("mq.codedCharSetId"));
        int bufferSize = Integer.parseInt(PropertiesLoader.getProperty("mq.bufferSize"));

        properties.put(CMQC.HOST_NAME_PROPERTY, host);
        properties.put(CMQC.PORT_PROPERTY, port);
        properties.put(CMQC.CHANNEL_PROPERTY, channel);
        properties.put(CMQC.USER_ID_PROPERTY, appUser);
        properties.put(CMQC.PASSWORD_PROPERTY, appPassword);

        try (FileInputStream inputStream = new FileInputStream(file)) {
            MQQueueManager queueManager = new MQQueueManager(qmgr, properties);
            MQQueue queue = queueManager.accessQueue(queueName, CMQC.MQOO_BROWSE);

            // Creating and setting values for RFH2 header
            String ifCobaString = "IF_COBA";
            MQRFH2 mqrfh2 = new MQRFH2();
            mqrfh2.setEncoding(encoding);
            mqrfh2.setCodedCharSetId(codedCharSetId);
            mqrfh2.setFormat(CMQC.MQFMT_NONE);
            mqrfh2.setFlags(0);
            mqrfh2.setNameValueCCSID(codedCharSetId);
            mqrfh2.setFieldValue(ifCobaString, "OriginatorApplication", "xxxxx");
            // ... (set other fields)

            MQMessage msgForSending = new MQMessage();

            // Writing RFH2 header and message payload
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            mqrfh2.write(new DataOutputStream(byteArrayOutputStream), 0, 0);
            msgForSending.write(byteArrayOutputStream.toByteArray());

            // Write message payload
            byte[] buf1 = new byte[bufferSize];
            int bytesRead;
            int bytesTotalSize = 0;

            while ((bytesRead = inputStream.read(buf1)) != -1) {
                msgForSending.write(buf1, 0, bytesRead);
                bytesTotalSize += bytesRead;
            }

            loggerManager.logInfo("Finished preparing message for MQ: " + bytesTotalSize + " bytes");

            // Set the RFH2 header in the message
            msgForSending.format = CMQC.MQFMT_RF_HEADER_2;
            msgForSending.messageFlags = CMQC.MQMF_SEGMENTATION_ALLOWED;
            msgForSending.messageFlags |= CMQC.MQMF_MSG_IN_GROUP;

            // Put the message to the queue
            queue.put(msgForSending);

            loggerManager.logInfo("Successfully sent message to MQ");

            // Close the queue and disconnect from the queue manager
            queue.close();
            queueManager.disconnect();
        } catch (IOException | MQException e) {
            loggerManager.logError("Error preparing or sending message to MQ: " + e.getMessage());
            throw e; // Rethrow the exception after logging
        }
    }
}
