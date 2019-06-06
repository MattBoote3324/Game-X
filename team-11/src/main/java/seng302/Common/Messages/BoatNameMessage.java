package seng302.Common.Messages;

import seng302.Common.Utils.MessageUtils;
import seng302.Server.Model.Race;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Class for doing name customizations to the server.
 */
public class BoatNameMessage extends Message implements MessageTypeHandler {

    private static final short MESSAGE_LENGTH = 37;
    public static final byte MESSAGE_TYPE = (byte) 106;
    private String boatName;
    private String shortName;

    private int sourceID;
    private MessageTypeHandler messageChain;


    /**
     * Constructor for sending a Boat name message
     *
     * @param id        Source id of the boat
     * @param shortName Short name of the baot
     * @param boatName  Long String for boat name. Less than 30 Characters
     */
    public BoatNameMessage(int id, String shortName, String boatName) {
        sourceID = id;
        this.shortName = shortName;
        this.boatName = boatName;
        ByteBuffer bodyBuffer = ByteBuffer.allocate(MESSAGE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        bodyBuffer.putInt(sourceID);
        bodyBuffer.put((byte) this.shortName.charAt(0));
        bodyBuffer.put((byte) this.shortName.charAt(1));
        bodyBuffer.put((byte) this.shortName.charAt(2));
        byte[] bName = new byte[30];
        System.arraycopy(boatName.getBytes(), 0, bName, 0, boatName.length());
        bodyBuffer.put(bName);

        setBytes(MessageUtils.generateMessageBytes(bodyBuffer.array(), MESSAGE_TYPE, MESSAGE_LENGTH));
    }

    /**
     * Parses a default message into a BoatNameMessage
     *
     * @param message Message to be used
     */
    public BoatNameMessage(Message message) {
        super(message.getBytes());

        ByteBuffer bodyBuffer = ByteBuffer.wrap(getBody()).order(ByteOrder.LITTLE_ENDIAN);
        sourceID = bodyBuffer.getInt();
        byte sName[] = new byte[3];
        bodyBuffer.get(sName, 0, 3);
        shortName = new String(sName);
        byte bName[] = new byte[30];
        bodyBuffer.get(bName, 0, 30);
        boatName = new String(bName);
        boatName = boatName.replaceAll("\\P{Print}", "");
    }

    public BoatNameMessage() {

    }

    public int getSourceID() {
        return sourceID;
    }

    public String getShortName() {
        return shortName;
    }

    public String getBoatName() {
        return boatName;
    }

    @Override
    public void updateRace(Race race) {
        race.getFleet().getBoat(getSourceID()).setName(getBoatName());
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
            return new BoatNameMessage(message);
        } else {
            // Send it on to the next in the chain
            if (this.messageChain != null) {
                return this.messageChain.getMessageType(message);
            }
        }
        return null;
    }
}
