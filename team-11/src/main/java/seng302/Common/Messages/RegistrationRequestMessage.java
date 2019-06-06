package seng302.Common.Messages;

import seng302.Common.Utils.MessageUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Model a Registration Request Message.
 * This message is used by a new client to register with a server.
 *
 * Hold all the necessary attributes / fields, for ease of use.
 */
public class RegistrationRequestMessage extends Message implements MessageTypeHandler {
    public static final byte MESSAGE_TYPE = (byte) 101;
    private static final short MESSAGE_LENGTH = 1;
    private byte clientType;
    private MessageTypeHandler messageChain;

    /**
     * Constructor used when sending.
     *
     * @param clientType The type of registration requested. 0 for spectator, 1 for player.
     */
    public RegistrationRequestMessage(byte clientType) {
        this.clientType = clientType;

        ByteBuffer bodyBuffer = ByteBuffer.allocate(MESSAGE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        bodyBuffer.put(clientType);

        setBytes(MessageUtils.generateMessageBytes(bodyBuffer.array(), MESSAGE_TYPE, MESSAGE_LENGTH));
    }

    /**
     * Constructor used after receiving a message and verifying its type.
     *
     * @param message The received message.
     */
    public RegistrationRequestMessage(Message message) {
        super(message.getBytes());

        ByteBuffer byteBuffer = ByteBuffer.wrap(getBody()).order(ByteOrder.LITTLE_ENDIAN);
        clientType = byteBuffer.get();
    }

    public RegistrationRequestMessage() {

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
            return new RegistrationRequestMessage(message);
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

    public byte getClientType() {
        return clientType;
    }
}
