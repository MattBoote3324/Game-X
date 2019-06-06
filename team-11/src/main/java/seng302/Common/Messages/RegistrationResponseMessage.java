package seng302.Common.Messages;

import seng302.Common.Boat;
import seng302.Common.CourseFeature;
import seng302.Common.Utils.MessageUtils;
import seng302.Server.Model.Race;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Model a Registration Response Message.
 * This message is used by the server to confirm to a client that they are connected, and give them an ID.
 *
 * Hold all the necessary attributes / fields, for ease of use.
 */
public class RegistrationResponseMessage extends Message implements MessageTypeHandler {
    public static final byte MESSAGE_TYPE = (byte) 102;
    private static final short MESSAGE_LENGTH = 5;
    private int sourceID;
    private byte status;
    private MessageTypeHandler messageChain;

    /**
     * Constructor used when sending.
     *
     * @param sourceID The ID that the server is assigning to the client.
     * @param status The status of the response. 0 for spectator success, 1 for player success, 10+ for failure.
     */
    public RegistrationResponseMessage(int sourceID, byte status) {
        this.sourceID = sourceID;
        this.status = status;

        ByteBuffer bodyBuffer = ByteBuffer.allocate(MESSAGE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        bodyBuffer.putInt(sourceID);
        bodyBuffer.put(status);

        setBytes(MessageUtils.generateMessageBytes(bodyBuffer.array(), MESSAGE_TYPE, MESSAGE_LENGTH));
    }

    /**
     * Constructor used after receiving a message and verifying its type.
     *
     * @param message The received message.
     */
    public RegistrationResponseMessage(Message message) {
        super(message.getBytes());

        ByteBuffer byteBuffer = ByteBuffer.wrap(getBody()).order(ByteOrder.LITTLE_ENDIAN);
        sourceID = byteBuffer.getInt();
        status = byteBuffer.get();
    }

    public RegistrationResponseMessage() {

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
            return new RegistrationResponseMessage(message);
        } else {
            // Send it on to the next in the chain
            if (this.messageChain != null) {
                return this.messageChain.getMessageType(message);
            }
        }
        return null;
    }

    /**
     * Handles the updating of the boat assign message
     *
     * @param race race to update
     */
    @Override
    public void updateRace(Race race) {
        // For now, a boat is made for the new client, and the boat is moved to the start line.

        // set a default name for this boat (will be overwritten by the boat name message)
        Boat boat = new Boat(sourceID, "Player " + sourceID, "P" + sourceID);
        boat.setHeading(180);
        boat.setSpeed(10);
        boat.setIsSailsOut(false);

        race.getFleet().addBoat(boat);

        int startLineID = race.getCourse().getCourseOrder().get(1).getCourseFeatureID();

        CourseFeature startLine = race.getCourse().getCourseFeatureById(startLineID);

        boat.setLatitude(startLine.getMidPoint().getLatitude());
        boat.setLongitude(startLine.getMidPoint().getLongitude());
    }

    ////////////////////////
    // GETTERS // SETTERS //
    ////////////////////////

    public int getSourceID() {
        return sourceID;
    }

    public byte getStatus() {
        return status;
    }
}