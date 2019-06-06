package seng302.Common.Messages;

import seng302.Common.Utils.MessageUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class HeartbeatMessage extends Message implements MessageTypeHandler {

    private static final short MESSAGE_LENGTH = 4;
    public static final byte MESSAGE_TYPE = (byte) 1;

    private int sequenceNum;
    private MessageTypeHandler messageChain;


    /**
     * Constructor used when sending
     * @param seqNo sequence number of heartbeat message
     */
    public HeartbeatMessage(int seqNo){
        this.sequenceNum = seqNo;

        ByteBuffer bodyBuffer = ByteBuffer.allocate(MESSAGE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        bodyBuffer.putInt(sequenceNum);

        setBytes(MessageUtils.generateMessageBytes(bodyBuffer.array(), MESSAGE_TYPE, MESSAGE_LENGTH));
    }


    /**
     * Constructor used when receiving
     * @param message message that has been received
     */
    public HeartbeatMessage(Message message){
        super(message.getBytes());

        ByteBuffer bodyBuffer = ByteBuffer.wrap(getBody()).order(ByteOrder.LITTLE_ENDIAN);

        sequenceNum = bodyBuffer.getInt();
    }

    public HeartbeatMessage() {

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
            return new HeartbeatMessage(message);
        } else {
            // Send it on to the next in the chain
            if (this.messageChain != null) {
                return this.messageChain.getMessageType(message);
            }
        }
        return null;
    }

    public int getSequenceNum() {
        return sequenceNum;
    }
}
