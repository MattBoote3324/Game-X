package seng302;

import org.junit.Test;
import seng302.Common.Messages.Message;
import seng302.Common.Utils.MessageUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class CRCTest {
    @Test
    public void crcPassTest() throws Exception {
        Checksum crc32 = new CRC32();

        byte[] headerBytes = new byte[15];
        headerBytes[0] = (byte) 0x47;
        headerBytes[1] = (byte) 0x83;
        headerBytes[2] = (byte) 0x0;

        ByteBuffer timeBuf = ByteBuffer.allocate(8);
        timeBuf.order(ByteOrder.LITTLE_ENDIAN);
        timeBuf.putLong(new Date().getTime());
        timeBuf.clear();
        timeBuf.get(headerBytes, 3, 6);

        ByteBuffer sourceBuf = ByteBuffer.allocate(4);
        sourceBuf.order(ByteOrder.LITTLE_ENDIAN);
        sourceBuf.putInt(0);
        sourceBuf.clear();
        sourceBuf.get(headerBytes, 9, 4);

        ByteBuffer lengthBuf = ByteBuffer.allocate(2);
        lengthBuf.order(ByteOrder.LITTLE_ENDIAN);
        lengthBuf.putShort((short) 22);
        lengthBuf.clear();
        lengthBuf.get(headerBytes, 13, 2);

        Message message = new Message();
        message.setHeaderFromBytes(headerBytes);
        crc32.update(headerBytes, 0, headerBytes.length);

        byte[] bodyBytes = "Hello, this is a test.".getBytes();
        message.setBody(bodyBytes);
        crc32.update(bodyBytes, 0, bodyBytes.length);

        byte[] crcBytes = new byte[4];
        ByteBuffer crcBuffer = ByteBuffer.allocate(4);
        crcBuffer.order(ByteOrder.LITTLE_ENDIAN);
        crcBuffer.putInt((int) crc32.getValue());
        crcBuffer.clear();
        crcBuffer.get(crcBytes, 0, 4);
        message.setCrc(crcBytes);

        assertTrue(MessageUtils.crc(message));
    }

    @Test
    public void crcFailTest() throws Exception {
        Checksum crc32 = new CRC32();

        byte[] headerBytes = new byte[15];
        headerBytes[0] = (byte) 0x47;
        headerBytes[1] = (byte) 0x83;
        headerBytes[2] = (byte) 0x0;

        ByteBuffer timeBuf = ByteBuffer.allocate(8);
        timeBuf.order(ByteOrder.LITTLE_ENDIAN);
        timeBuf.putLong(new Date().getTime());
        timeBuf.clear();
        timeBuf.get(headerBytes, 3, 6);

        ByteBuffer sourceBuf = ByteBuffer.allocate(4);
        sourceBuf.order(ByteOrder.LITTLE_ENDIAN);
        sourceBuf.putInt(0);
        sourceBuf.clear();
        sourceBuf.get(headerBytes, 9, 4);

        ByteBuffer lengthBuf = ByteBuffer.allocate(2);
        lengthBuf.order(ByteOrder.LITTLE_ENDIAN);
        lengthBuf.putShort((short) 28);
        lengthBuf.clear();
        lengthBuf.get(headerBytes, 13, 2);

        Message message = new Message();
        message.setHeaderFromBytes(headerBytes);
        crc32.update(headerBytes, 0, headerBytes.length);

        byte[] bodyBytes = "Hello, this is another test.".getBytes();
        message.setBody(bodyBytes);

        // The CRC is being calculated incorrectly here, since it ignores the last byte of the message.
        crc32.update(bodyBytes, 0, bodyBytes.length - 1);

        byte[] crcBytes = new byte[4];
        ByteBuffer crcBuffer = ByteBuffer.allocate(4);
        crcBuffer.order(ByteOrder.LITTLE_ENDIAN);
        crcBuffer.putInt((int) crc32.getValue());
        crcBuffer.clear();
        crcBuffer.get(crcBytes, 0, 4);
        message.setCrc(crcBytes);

        assertFalse(MessageUtils.crc(message));
    }
}
