package com.coba;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.headers.MQRFH2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

public class MsgToMQ {

    private final LoggerManager loggerManager;

    public MsgToMQ(LoggerManager loggerManager) {
        this.loggerManager = loggerManager;
    }

    /**
     * @param messageContent
     */
    public void prepareAndSendToMQ(String messageContent) {
        // Load application properties
        if (!PropertiesLoader.loadProperties()) {
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

        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(CMQC.HOST_NAME_PROPERTY, host);
        properties.put(CMQC.PORT_PROPERTY, port);
        properties.put(CMQC.CHANNEL_PROPERTY, channel);
        properties.put(CMQC.USER_ID_PROPERTY, appUser);
        properties.put(CMQC.PASSWORD_PROPERTY, appPassword);

        try (FileInputStream inputStream = new FileInputStream(new File(messageContent))) {
            MQQueueManager queueManager = new MQQueueManager(qmgr, properties);
            MQQueue queue = queueManager.accessQueue(queueName, CMQC.MQOO_BROWSE);

            String ifCobaString = "IF_COBA";
            MQRFH2 mqrfh2 = new MQRFH2();
            int codedCharSetId = 1208; // Replace magic number with a constant
            mqrfh2.setEncoding(CMQC.MQENC_NATIVE);
            mqrfh2.setCodedCharSetId(codedCharSetId);
            mqrfh2.setFormat(CMQC.MQFMT_NONE);
            mqrfh2.setFlags(0);
            mqrfh2.setNameValueCCSID(1208);
            mqrfh2.setFieldValue(ifCobaString, "OriginatorApplication", "xxxxx");
            mqrfh2.setFieldValue(ifCobaString, "UserReference", "xxxxx");
            String requestor = "o=" + PropertiesLoader.getProperty("mq.requestorOrg") + ",o=swift";
            mqrfh2.setFieldValue(ifCobaString, "Requestor", requestor);
            mqrfh2.setFieldValue(ifCobaString, "Responder",
                    "o=" + PropertiesLoader.getProperty("mq.responderOrg") + ",o=swift");
            mqrfh2.setFieldValue(ifCobaString, "Service", PropertiesLoader.getProperty("mq.service"));
            mqrfh2.setFieldValue(ifCobaString, "RequestType", PropertiesLoader.getProperty("mq.requestType"));
            mqrfh2.setFieldValue(ifCobaString, "Compression", PropertiesLoader.getProperty("mq.compression"));
            mqrfh2.setFieldValue(ifCobaString, "FileName", PropertiesLoader.getProperty("mq.fileName"));

            MQMessage msgForSending = new MQMessage();
            MQPutMessageOptions putMessageOptions = new MQPutMessageOptions();
            int bytesRead;
            int bytesTotalSize = 0;
            byte[] buf1 = new byte[8192]; // Replace magic number with a constant

            while ((bytesRead = inputStream.read(buf1)) != -1) {
                msgForSending.write(buf1, 0, bytesRead);
                bytesTotalSize += bytesRead;
            }

            loggerManager.logInfo("Finished preparing message for MQ: " + bytesTotalSize + " bytes");

            // Set the RFH2 header in the message
            msgForSending.format = CMQC.MQFMT_RF_HEADER_2;
            msgForSending.messageFlags = CMQC.MQMF_SEGMENTATION_ALLOWED;
            msgForSending.messageFlags |= CMQC.MQMF_MSG_IN_GROUP;

            msgForSending.write(mqrfh2.write());
            queue.put(msgForSending);

            loggerManager.logInfo("Successfully sent message to MQ");

            queue.close();
            queueManager.disconnect();
        } catch (IOException | MQException e) {
            loggerManager.logError("Error preparing or sending message to MQ: " + e.getMessage());
        }
    }
}
