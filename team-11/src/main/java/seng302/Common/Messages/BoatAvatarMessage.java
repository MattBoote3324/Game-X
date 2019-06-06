package seng302.Common.Messages;

import seng302.Common.GodType;
import seng302.Common.Utils.MessageUtils;
import seng302.Server.Model.Race;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by gmc125 on 29/08/17.
 */
public class BoatAvatarMessage extends Message implements MessageTypeHandler {
    private static final short MESSAGE_LENGTH = 37;
    public static final byte MESSAGE_TYPE = (byte) 206;

    private int sourceID;

    private int type;
    private MessageTypeHandler messageChain;


    public BoatAvatarMessage(int id, int avatarType) {
        sourceID = id;
        type = avatarType;
        ByteBuffer bodyBuffer = ByteBuffer.allocate(MESSAGE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        bodyBuffer.putInt(sourceID);
        bodyBuffer.put((byte) type);

        setBytes(MessageUtils.generateMessageBytes(bodyBuffer.array(), MESSAGE_TYPE, MESSAGE_LENGTH));

    }

    public BoatAvatarMessage(Message message) {
        super(message.getBytes());
        ByteBuffer bodyBuffer = ByteBuffer.wrap(getBody()).order(ByteOrder.LITTLE_ENDIAN);
        sourceID = bodyBuffer.getInt();
        type = bodyBuffer.get();
    }

    public BoatAvatarMessage() {

    }

    public int getSourceID() {
        return sourceID;
    }

    public int getType() {
        return type;
    }

    @Override
    public void updateRace(Race race) {
        String name = race.getFleet().getBoat(getSourceID()).getName();
        int avatarType = getType();
        race.getFleet().getBoat(getSourceID()).setGreekGod(GodType.values()[avatarType].getGod());
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
            return new BoatAvatarMessage(message);
        } else {
            // Send it on to the next in the chain
            if (this.messageChain != null) {
                return this.messageChain.getMessageType(message);
            }
        }
        return null;
    }
}
