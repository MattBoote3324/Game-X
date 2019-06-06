package seng302.Common.Messages;

/**
 * Interface for the Abstract message handler class,
 */
public interface MessageTypeHandler {

    /**
     * Sets the next message handler in the chain
     *
     * @param nextHandler next handler in the chain
     */
    void setNextMessageHandler(MessageTypeHandler nextHandler);

    /**
     * Returns a new Message from the generic message passed in
     *
     * @param message Generic message
     * @return message of class type or null if it does not match
     */
    Message getMessageType(Message message);
}
