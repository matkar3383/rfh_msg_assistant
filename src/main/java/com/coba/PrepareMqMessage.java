package com.coba;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.headers.MQRFH2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrepareMqMessage {

    public static void main(String[] args) throws MQException {
        // Odczytaj parametry połączenia i dane z pliku application.properties
        Properties properties = readProperties("application.properties");

        String host = properties.getProperty("mq.host");
        int port = Integer.parseInt(properties.getProperty("mq.port"));
        String channel = properties.getProperty("mq.channel");
        String user = properties.getProperty("mq.user");
        String password = properties.getProperty("mq.password");

        String xmlFilePath = properties.getProperty("xml.file.path");
        String xmlContent = readXmlFile(xmlFilePath);

        // Przygotowanie wiadomości
        MQQueueManager queueManager = prepareConnection(host, port, channel, user, password);
        MQQueue queue = prepareQueue(queueManager, properties.getProperty("mq.queue.name"));

        // Tworzenie nagłówka IF_COBA
        MQRFH2 mqrfh2 = createIfCobaHeader(xmlContent);
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
        Hashtable<String, Object> properties = new Hashtable<>();
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

    private static MQRFH2 createIfCobaHeader(String xmlContent) {
        MQRFH2 mqrfh2 = new MQRFH2();
        mqrfh2.setEncoding(CMQC.MQENC_NATIVE);
        mqrfh2.setCodedCharSetId(CMQC.MQCCSI_INHERIT);
        mqrfh2.setFormat(CMQC.MQFMT_NONE);
        mqrfh2.setFlags(0);
        mqrfh2.setNameValueCCSID(1208);

        // Odczytaj dane z pliku XML
        
        /* 
        <IF_COBA>
            <Direction>Input</Direction>
            <OriginatorApplication>COW01</OriginatorApplication>
            <Owner>COBADEF0</Owner>
            <UserReference>COW.HJX-CR01P8-1113121059-5.</UserReference>
            <Requestor>o=cobadeff,o=swift</Requestor>
            <Responder>cn=central,cn=serv,o=ebapfrpp,o=swift</Responder>
            <Service>eba.step2!pu1</Service>
            <RequestType>pacs.xxx.sct.r.icf</RequestType>
            <DeliveryMode>RealTime</DeliveryMode>
            <DelNotRequest>TRUE</DelNotRequest>
            <DelNotReceiverDN>o=cobadeff,o=swift</DelNotReceiverDN>
            <Compression>None</Compression>
            <NRIndicator>TRUE</NRIndicator>
            <FileName>S202SCTCOBADEF02311159990804.I</FileName>
            <FileReference>S202SCTCOBADEF02311159990804.I</FileReference>
            <DuplicateCheckOverride>Skip</DuplicateCheckOverride>
        </IF_COBA>
        */
        String directionValue = readXmlValue(xmlContent, "Direction");
        String xxxxValue = readXmlValue(xmlContent, "XXXX");

        try {
            // Ustawienia nagłówka IF_COBA
            mqrfh2.setFieldValue("IF_COBA", "Direction", directionValue);
        } catch (IOException ex) {
            Logger.getLogger(PrepareMqMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            mqrfh2.setFieldValue("IF_COBA", "OriginatorApplication", xxxxValue);
        } catch (IOException ex) {
            Logger.getLogger(PrepareMqMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            mqrfh2.setFieldValue("IF_COBA", "Owner", xxxxValue);
        } catch (IOException ex) {
            Logger.getLogger(PrepareMqMessage.class.getName()).log(Level.SEVERE, null, ex);
        }

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

    private static String readXmlValue(String xmlContent, String tagName) {
        // Implementacja odczytu wartości dla danego tagu z pliku XML
        // Tu musisz dostosować logikę odczytu wartości z pliku XML
        // Możesz użyć biblioteki do parsowania XML, np. DOM lub JAXB
        // W tym przykładzie zakładam prosty odczyt, ale w rzeczywistym przypadku będziesz musiał dostosować to do struktury twojego pliku XML
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        int startIndex = xmlContent.indexOf(startTag);
        int endIndex = xmlContent.indexOf(endTag, startIndex);
        if (startIndex != -1 && endIndex != -1) {
            return xmlContent.substring(startIndex + startTag.length(), endIndex);
        } else {
            throw new RuntimeException("Error reading value for tag: " + tagName);
        }
    }

    private static Properties readProperties(String propertiesFile) {
        Properties properties = new Properties();
        try (InputStream input = PrepareMqMessage.class.getClassLoader().getResourceAsStream(propertiesFile)) {
            if (input == null) {
                System.out.println("Sorry, unable to find " + propertiesFile);
                return properties;
            }
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
