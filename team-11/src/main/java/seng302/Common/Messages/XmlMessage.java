package seng302.Common.Messages;

import org.xml.sax.SAXException;
import seng302.Client.ClientMain;
import seng302.Common.Course;
import seng302.Common.Fleet;
import seng302.Common.Regatta;
import seng302.Common.Utils.MessageUtils;
import seng302.Common.XMLParser;
import seng302.Server.Model.Race;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Model an XML message.
 *
 * Hold all the necessary attributes / fields, for ease of use.
 */
public class XmlMessage extends Message implements MessageTypeHandler {
    private static final byte MESSAGE_TYPE = (byte) 26;
    public static final byte REGATTA_SUBTYPE = (byte) 5;
    public static final byte RACE_SUBTYPE = (byte) 6;
    public static final byte BOAT_SUBTYPE = (byte) 7;

    private static final short FIXED_LENGTH = 14;  // The absolute minimum byte length of an XML message.
    private Message message;
    private byte messageVersionNumber = (byte) 1;  // Not needed.
    private short ackNumber = (byte) 1;            // Not needed.
    private long timeStamp;
    private byte xmlMsgSubType;
    private short sequenceNumber;
    private short xmlMsgLength;
    private String xmlMessage;
    private Regatta regatta;
    private MessageTypeHandler messageChain;

    /**
     *  Constructor used when sending.
     *
     * @param xmlMsgSubType The type of XML message. Could be regatta, race, or boat.
     * @param sequenceNumber The amount of XML messages of this subtype have been sent.
     * @param xmlMessage The actual XML null terminated string.
     */
    public XmlMessage(byte xmlMsgSubType, short sequenceNumber, String xmlMessage) {
        this.timeStamp = System.currentTimeMillis();
        this.xmlMsgSubType = xmlMsgSubType;
        this.sequenceNumber = sequenceNumber;
        this.xmlMsgLength = (short) xmlMessage.length();
        this.xmlMessage = xmlMessage;

        ByteBuffer bodyBuffer = ByteBuffer.allocate(FIXED_LENGTH + xmlMsgLength).order(ByteOrder.LITTLE_ENDIAN);
        bodyBuffer.put(messageVersionNumber);
        bodyBuffer.putShort(ackNumber);
        bodyBuffer.put(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(timeStamp).array(), 0, 6);
        bodyBuffer.put(xmlMsgSubType);
        bodyBuffer.putShort(sequenceNumber);
        bodyBuffer.putShort(xmlMsgLength);
        bodyBuffer.put(xmlMessage.getBytes(Charset.forName("UTF-8")));

        setBytes(MessageUtils.generateMessageBytes(bodyBuffer.array(), MESSAGE_TYPE, (short) (FIXED_LENGTH + xmlMsgLength)));
    }

    /**
     * Constructor used after receiving a message and verifying its type.
     *
     * @param message The received message.
     */
    public XmlMessage(Message message) {
        super(message.getBytes());

        ByteBuffer bodyBuffer = ByteBuffer.wrap(getBody()).order(ByteOrder.LITTLE_ENDIAN);
        byte[] numberContainer = new byte[Long.BYTES];

        messageVersionNumber = bodyBuffer.get();
        bodyBuffer.get(numberContainer, 0, 6);  // 6 bytes to hold the time field.
        ackNumber = bodyBuffer.getShort();
        timeStamp = ByteBuffer.wrap(numberContainer).order(ByteOrder.LITTLE_ENDIAN).getLong();
        xmlMsgSubType = bodyBuffer.get();
        sequenceNumber = bodyBuffer.getShort();
        xmlMsgLength = bodyBuffer.getShort();
        byte[] xmlMessageBytes = new byte[xmlMsgLength];
        bodyBuffer.get(xmlMessageBytes, 0, xmlMsgLength);
        xmlMessage = new String(xmlMessageBytes, Charset.forName("UTF-8"));
    }

    public XmlMessage() {

    }

    /**
     * Updates a given race according to this XML message.
     *
     * Client side method.
     *
     * @param race The race to update.
     */
    @Override
    public void updateRace(Race race) {
        InputStream xmlMessageStream = new ByteArrayInputStream(xmlMessage.getBytes(Charset.forName("UTF-8")));
        try {
            XMLParser xmlParser = new XMLParser(xmlMessageStream);
            switch (getXmlMsgSubType()) {
                case XmlMessage.RACE_SUBTYPE:
                    xmlParser.parseCourse();
                    race.setCourse(new Course(xmlParser.getCourseFeatures(), xmlParser.getBoundaryPoints(), xmlParser.getCourseOrder()));
                    break;
                case XmlMessage.REGATTA_SUBTYPE:
                    xmlParser.parseRegattaXML();
                    regatta = new Regatta(
                            xmlParser.getRegattaID(),
                            xmlParser.getRegattaName(),
                            xmlParser.getCourseName(),
                            xmlParser.getCentralLatitude(),
                            xmlParser.getCentralLongitude(),
                            xmlParser.getCentralAltitude(),
                            xmlParser.getUtc(),
                            xmlParser.getMagVar()
                    );
                    break;
                case XmlMessage.BOAT_SUBTYPE:
                    xmlParser.parseForBoatList();
                    int userId = race.getFleet().getUserAssignId();
                    race.setFleet(new Fleet(xmlParser.getBoatList()));
                    race.getFleet().setUserAssignId(userId);
                    race.getFleet().getUserAssignBoat().setAssigned(true);
                    ClientMain.lobbyViewController.setTable(race.getFleet());
                    break;
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the next in the chain of message handlers
     *
     * @param nextHandler next handler in the chain
     */
    @Override
    public void setNextMessageHandler(MessageTypeHandler nextHandler) {
        this.messageChain = nextHandler;
    }

    /**
     * If the message matches this classes message type, then the class will return a
     * new message of this type, part of the chain of responsiblity
     *
     * @param message raw message type
     * @return a message type matching this class
     */
    @Override
    public Message getMessageType(Message message) {
        if (message.getHeader().getMessageType() == MESSAGE_TYPE) {
            return new XmlMessage(message);
        } else {
            // Send it on to the next in the chain
            if (this.messageChain != null) {
                return this.messageChain.getMessageType(message);
            }
        }
        return null;
    }

    ////////////////////////
    // GETTERS // SETTERS //
    ////////////////////////

    public byte getMessageVersionNumber() {
        return messageVersionNumber;
    }

    public short getAckNumber() {
        return ackNumber;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public byte getXmlMsgSubType() {
        return xmlMsgSubType;
    }

    public short getSequenceNumber() {
        return sequenceNumber;
    }

    public short getXmlMsgLength() {
        return xmlMsgLength;
    }

    public String getXmlMessage() {
        return xmlMessage;
    }

    public String getRegattaName() {
        if(regatta == null) return "";
        else return regatta.getRegattaName();
    }

}
