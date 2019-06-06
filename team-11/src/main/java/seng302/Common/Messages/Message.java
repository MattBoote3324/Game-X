package seng302.Common.Messages;

import seng302.Common.Utils.MessageUtils;
import seng302.Server.Model.Race;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 *
 */
public class Message {
    private byte[] bytes;  // All the bytes.
    private Header header;
    private byte[] body;
    private byte[] crc;

    public Message() {

    }

    /**
     * Constructor used when sending.
     * Should only be used via super(bytes) call in inheriting classes.
     *
     * @param bytes The bytes to send.
     */
    public Message(byte[] bytes) {
        this.bytes = bytes;
        setUpFromBytes();
    }

    /**
     * Constructor used when receiving a message if you do not yet know the type.
     *
     * @param bufferedInputStream The receiving input stream.
     * @throws IOException In case of error.
     */
    public Message(BufferedInputStream bufferedInputStream) throws IOException {
        byte[] headerBytes = new byte[MessageUtils.HEADER_LENGTH];
        bufferedInputStream.read(headerBytes, 0, MessageUtils.HEADER_LENGTH);

        header = new Header(headerBytes);

        if (header.isValid) {
            body = new byte[header.messageLength];
            bufferedInputStream.read(body, 0, header.messageLength);

            crc = new byte[MessageUtils.CRC_LENGTH];
            bufferedInputStream.read(crc, 0, MessageUtils.CRC_LENGTH);
        }
    }

    /**
     * Update a given race model based off the message.
     *
     * @param race The race model to update.
     */
    public void updateRace(Race race) {
    }

    public byte[] getBytes() {
        if (bytes != null) {
            return bytes;
        } else {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                byteArrayOutputStream.write(header.getBytes());
                byteArrayOutputStream.write(body);
                byteArrayOutputStream.write(crc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bytes = byteArrayOutputStream.toByteArray();
            return bytes;
        }
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        setUpFromBytes();
    }

    private void setUpFromBytes() {
        byte[] headerBytes = Arrays.copyOfRange(bytes, 0, MessageUtils.HEADER_LENGTH);
        header = new Header(headerBytes);

        body = Arrays.copyOfRange(bytes, MessageUtils.HEADER_LENGTH, MessageUtils.HEADER_LENGTH + header.messageLength);

        crc = Arrays.copyOfRange(bytes, MessageUtils.HEADER_LENGTH, MessageUtils.HEADER_LENGTH + header.messageLength + MessageUtils.CRC_LENGTH);
    }

    public Header getHeader() {
        return header;
    }

    public void setHeaderFromBytes(byte[] headerBytes) {
        this.header = new Header(headerBytes);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getCrc() {
        return crc;
    }

    public void setCrc(byte[] crc) {
        this.crc = crc;
    }

    public static class Header {
        private byte[] bytes = null;
        private byte syncByte1 = MessageUtils.SYNC_BYTE_1;
        private byte syncByte2 = MessageUtils.SYNC_BYTE_2;
        private byte messageType;
        private long timeStamp;
        private int sourceID;
        private short messageLength;
        private boolean isValid;

        /**
         * Constructor for the header of the packet.
         *
         * @param bytes the bytes
         */
        Header(byte[] bytes) {
            Byte syncByte1 = bytes[0];
            Byte syncByte2 = bytes[1];
            if (syncByte1.equals(MessageUtils.SYNC_BYTE_1) && syncByte2.equals(MessageUtils.SYNC_BYTE_2)) {
                this.bytes = bytes;
                // Do the conversions
                this.syncByte1 = bytes[0];
                this.syncByte2 = bytes[1];
                this.messageType = bytes[2];
                ByteBuffer byteBuffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 3, 9));
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                ByteBuffer longBuf = ByteBuffer.allocate(8);
                longBuf.order(ByteOrder.LITTLE_ENDIAN);
                longBuf.put(byteBuffer.get(longBuf.array(), 0, 6));
                this.timeStamp = longBuf.getLong();
                byteBuffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 9, 13));
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                this.sourceID = byteBuffer.getInt();
                byteBuffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 13, 15));
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                this.messageLength = byteBuffer.getShort();
                isValid = true;
            } else {
                isValid = false;
            }
        }

        public byte[] getBytes() {
            return bytes;
        }

        public byte getSyncByte1() {
            return syncByte1;
        }

        public byte getSyncByte2() {
            return syncByte2;
        }

        public byte getMessageType() {
            return messageType;
        }

        public void setMessageType(byte type) {
            messageType = type;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public int getSourceID() {
            return sourceID;
        }

        public short getMessageLength() {
            return messageLength;
        }

        public boolean isValid() {
            return isValid;
        }
    }
}
