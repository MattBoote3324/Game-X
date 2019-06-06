package seng302.Common.Messages;

import seng302.Common.Boat;
import seng302.Common.Utils.MessageUtils;
import seng302.Server.Model.Race;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * The message for yacht event codes, which are related to penalties
 */
public class YachtEventCodeMessage extends Message implements MessageTypeHandler {
    private static final byte MESSAGE_TYPE = (byte) 26;
    private byte messageVersionNumber;
    private long time;
    private short ackNum;
    private int raceId;
    private int destinationSrcId;
    private int incidentId;
    private byte eventId;

    private static int ackNo = 0;
    private MessageTypeHandler messageChain;

    public YachtEventCodeMessage(byte[] bytes){
        messageVersionNumber = bytes[0];
        ByteBuffer buf = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 1, 7));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer longBuf = ByteBuffer.allocate(8);
        longBuf.order(ByteOrder.LITTLE_ENDIAN);
        longBuf.put(buf.get(longBuf.array(), 0, 6));
        time = longBuf.getLong();
        buf = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 7, 9));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        ackNum = buf.getShort();
        buf = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 9, 13));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        raceId = buf.getInt();
        buf = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 13, 17));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        destinationSrcId = buf.getInt();
        buf = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 17, 21));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        incidentId = buf.getInt();
        eventId = bytes[21];
    }

    /**
     * Constructor to help us make a new yacht event code message to be sent by the mockgenerator.
     * @param subjectYacht Boat that we are sending data for
     * @param myIncidentId unique identifier ties incident to associated yacht events
     * @param myEventID the id of the event
     */
    public YachtEventCodeMessage(Boat subjectYacht, int myIncidentId, int myEventID) {
        this.destinationSrcId = subjectYacht.getSourceID();
        this.incidentId = myIncidentId;
        this.eventId = (byte)myEventID;
    }

    public YachtEventCodeMessage() {

    }

    /**
     * Method to generate Binary Data for the Boat Location details.
     * A byte buffer is filled with the data
     *
     * **Note that currently bytes 28-56 are not being allocated separately as we are not currently adding any specific
     * data for these fields.**
     *
     * @return byte buffer contianing the boat location data in binary form
     */
    public ByteBuffer generateYachtEventMessage() {

        ByteBuffer finalBuffer = ByteBuffer.allocate(22);

        MessageUtils.addByteToByteBuffer(finalBuffer, (byte) 2); //message version, will always be 2

        finalBuffer.put(MessageUtils.generateCurrentTimeBytes(),0 ,6);

        MessageUtils.addShortToByteBuffer(finalBuffer, (short) ackNo++);

        MessageUtils.addIntToByteBuffer(finalBuffer, 0); //Race ID

        MessageUtils.addIntToByteBuffer(finalBuffer, destinationSrcId);

        MessageUtils.addIntToByteBuffer(finalBuffer, incidentId);

        MessageUtils.addByteToByteBuffer(finalBuffer, eventId);

        return finalBuffer;
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
        if (message.getHeader().getMessageType() != MESSAGE_TYPE) {
            if (this.messageChain != null) {
                return this.messageChain.getMessageType(message);
            }
        }
        return null;
    }

    /**
     * Updates the Race model with a Yacht Event Code Message
     *
     * @param race race that should be updated
     */
    @Override
    public void updateRace(Race race) {
    }

    public byte getMessageVersionNumber() {
        return messageVersionNumber;
    }

    public long getTime() {
        return time;
    }

    public int getAckNum() {
        return ackNum;
    }

    public int getRaceId() {
        return raceId;
    }

    public int getDestinationSrcId() {
        return destinationSrcId;
    }

    public int getIncidentId() {
        return incidentId;
    }

    public byte getEventId() {
        return eventId;
    }

}
