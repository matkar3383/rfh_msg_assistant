package com.coba;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.headers.MQRFH2;
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
     * Prepare and send message to MQ.
     *
     * @param file The file to be sent as a message.
     */
    public void prepareAndSendToMQ(String file) {
        try {
            // Load application properties
            PropertiesLoader.loadProperties();
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

            Hashtable<String, Object> properties = new Hashtable<>();
            properties.put(CMQC.HOST_NAME_PROPERTY, host);
            properties.put(CMQC.PORT_PROPERTY, port);
            properties.put(CMQC.CHANNEL_PROPERTY, channel);
            properties.put(CMQC.USER_ID_PROPERTY, appUser);
            properties.put(CMQC.PASSWORD_PROPERTY, appPassword);

            File fileToProcess = new File(file);
            if (!fileToProcess.exists()) {
                loggerManager.logError("File " + file + " does not exist");
                return;
            }

            try (FileInputStream inputStream = new FileInputStream(fileToProcess)) {
                MQQueueManager queueManager = new MQQueueManager(qmgr, properties);
                MQQueue queue = queueManager.accessQueue(queueName, CMQC.MQOO_BROWSE);

                String ifCobaString = "IF_COBA";
                MQRFH2 mqrfh2 = new MQRFH2();
                mqrfh2.setEncoding(encoding);
                mqrfh2.setCodedCharSetId(codedCharSetId);
                mqrfh2.setFormat(CMQC.MQFMT_NONE);
                mqrfh2.setFlags(0);
                mqrfh2.setNameValueCCSID(codedCharSetId);
                mqrfh2.setFieldValue(ifCobaString, "Direction", PropertiesLoader.getProperty("ifcoba.direction"));
                mqrfh2.setFieldValue(ifCobaString, "OriginatorApplication",
                        PropertiesLoader.getProperty("ifcoba.origAppl"));
                mqrfh2.setFieldValue(ifCobaString, "Owner", PropertiesLoader.getProperty("ifcoba.owner"));
                mqrfh2.setFieldValue(ifCobaString, "Requestor", PropertiesLoader.getProperty("ifcoba.requestor"));
                mqrfh2.setFieldValue(ifCobaString, "Responder", PropertiesLoader.getProperty("ifcoba.responder"));
                mqrfh2.setFieldValue(ifCobaString, "Service", PropertiesLoader.getProperty("ifcoba.service"));
                mqrfh2.setFieldValue(ifCobaString, "RequestType", PropertiesLoader.getProperty("ifcoba.requestType"));
                mqrfh2.setFieldValue(ifCobaString, "DeliveryMode",
                        PropertiesLoader.getProperty("ifcoba.deliveryMode"));
                mqrfh2.setFieldValue(ifCobaString, "DelNotRequest",
                        PropertiesLoader.getProperty("ifcoba.delNotRequest"));
                mqrfh2.setFieldValue(ifCobaString, "DelNotReceiverDN",
                        PropertiesLoader.getProperty("ifcoba.DelNotReceiverDN"));
                mqrfh2.setFieldValue(ifCobaString, "Compression", PropertiesLoader.getProperty("ifcoba.compression"));
                mqrfh2.setFieldValue(ifCobaString, "NRIndicator", PropertiesLoader.getProperty("ifcoba.NRIndicator"));
                mqrfh2.setFieldValue(ifCobaString, "DuplicateCheckOverride",
                        PropertiesLoader.getProperty("ifcoba.duplicateCheckOverride"));

                MQMessage msgForSending = new MQMessage();
                // ... (Pozostała część kodu)

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

                queue.put(msgForSending);

                loggerManager.logInfo("Successfully sent message to MQ: " + fileToProcess.getName());

                queue.close();
                queueManager.disconnect();
            }
        } catch (MQException mqe) {
            loggerManager.logError("MQException while preparing or sending message to MQ: " + mqe.getMessage());
        } catch (IOException ioe) {
            loggerManager.logError("IOException while preparing or sending message to MQ: " + ioe.getMessage());
        } catch (NumberFormatException e) {
            loggerManager.logError("Unexpected error while preparing or sending message to MQ: " + e.getMessage());
        }
    }
}
