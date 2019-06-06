package seng302.Common.Utils;

import seng302.Common.Messages.Message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Packet utilities.
 */
public class MessageUtils {
    public static final int HEADER_LENGTH = 15;
    public static final int TIMESTAMP_LENGTH = 6;
    public static final int CRC_LENGTH = 4;

    public static final byte SYNC_BYTE_1 = (byte) 0x47;
    public static final byte SYNC_BYTE_2 = (byte) 0x83;

    /**
     * Generate a byte buffer containing a message.
     * The byte buffer includes the message header and crc.
     *
     * @param bodyBytes   The message body.
     * @param messageType   The message type.
     * @param messageLength The message length.
     * @return A ByteBuffer containing a message, header included.
     */
    public static byte[] generateMessageBytes(byte[] bodyBytes, byte messageType, short messageLength) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER_LENGTH + messageLength + CRC_LENGTH);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(generateHeaderBytes(messageType, messageLength));
        byteBuffer.put(bodyBytes);
        Checksum crc32 = new CRC32();
        crc32.update(byteBuffer.array(), 0, byteBuffer.array().length - CRC_LENGTH);
        ByteBuffer crcByteBuffer = ByteBuffer.allocate(CRC_LENGTH);
        crcByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        crcByteBuffer.putInt((int) crc32.getValue());
        crcByteBuffer.position(0);
        byteBuffer.position(messageLength + HEADER_LENGTH);
        byteBuffer.put(crcByteBuffer.array());
        byteBuffer.position(0);
        return byteBuffer.array();
    }

    /**
     * Generate a byte buffer containing a message's header.
     *
     * @param messageType The message type.
     * @return A ByteBuffer containing a message's header.
     */
    private static byte[] generateHeaderBytes(byte messageType, short messageLength) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER_LENGTH);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(SYNC_BYTE_1);
        byteBuffer.put(SYNC_BYTE_2);
        byteBuffer.put(messageType);
        byteBuffer.put(generateCurrentTimeBytes(), 0, TIMESTAMP_LENGTH);
        byteBuffer.putInt(0); // A source ID.
        byteBuffer.putShort(messageLength);
        byteBuffer.position(0);
        return byteBuffer.array();
    }

    /**
     * Perform a cyclic redundancy check (CRC) for a given Message.
     *
     * @param message The message to check.
     * @return true if the packet passed CRC, false otherwise.
     */
    public static boolean crc(Message message) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(message.getCrc());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int crcInt = byteBuffer.getInt();

        byte[] headerBytes = message.getHeader().getBytes();
        byte[] bodyBytes = message.getBody();

        // Combine the header and body.
        byte[] pktBytesToCheck = new byte[headerBytes.length + bodyBytes.length];
        System.arraycopy(headerBytes, 0, pktBytesToCheck, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, pktBytesToCheck, headerBytes.length, bodyBytes.length);

        Checksum crc32 = new CRC32();
        // Calculate the CRC value ourselves to compare against.
        crc32.update(pktBytesToCheck, 0, pktBytesToCheck.length);

        String ourCrcHex = Long.toHexString(crc32.getValue());
        String messageCrcHex = Integer.toHexString(crcInt);

        return ourCrcHex.equalsIgnoreCase(messageCrcHex);
    }

    /**
     * Method extracting repeated code used to add bytes to the byte buffer
     * @param destination the buffer for the final Boat Location message to be sent
     * @param value the value to be added to the final buffer
     */
    public static void addByteToByteBuffer(ByteBuffer destination, byte value) {
        ByteBuffer buf = ByteBuffer.allocate(1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(value);
        buf.position(0);
        destination.put(buf.array());
    }

    /**
     * Method extracting repeated code used to add shorts to the byte buffer
     * @param destination the buffer for the final Boat Location message to be sent
     * @param value the value to be added to the final buffer
     */
    public static void addShortToByteBuffer(ByteBuffer destination, short value) {
        ByteBuffer buf = ByteBuffer.allocate(Short.BYTES);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort(value);
        buf.position(0);
        destination.put(buf.array());
    }

    /**
     * Method extracting repeated code used to add values to the byte buffer
     * @param destination the buffer for the final Boat Location message to be sent
     * @param value the value to be added to the final buffer
     */
    public static void addIntToByteBuffer(ByteBuffer destination, int value) {
        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(value);
        buf.position(0);
        destination.put(buf.array());
    }

    /**
     * Generates the Number of milliseconds from Jan 1, 1970 (as specified by AC35 protocol).
     * Uses a bit mask to remove the 2 MSB (as the time portion of the binary message takes up 6 bytes not 8)
     * @return timebuf- Byte buffer length 8 containing the number of milliseconds
     */
    public static byte[] generateCurrentTimeBytes(){
        long ms = System.currentTimeMillis();
        ByteBuffer timeBuffer = ByteBuffer.allocate(Long.BYTES);
        timeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        timeBuffer.putLong(ms);
        timeBuffer.position(0);
        return timeBuffer.array();
    }
}