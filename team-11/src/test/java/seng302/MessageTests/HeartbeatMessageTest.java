package seng302.MessageTests;

import org.junit.Assert;
import org.junit.Test;
import seng302.Common.Messages.HeartbeatMessage;



public class HeartbeatMessageTest {

    @Test
    public void testHeartbeatMessage(){
        int seqNo = 1;

        HeartbeatMessage heartbeatMessage = new HeartbeatMessage(seqNo);
        HeartbeatMessage heartbeatMessageFromBytes = new HeartbeatMessage(heartbeatMessage);

        Assert.assertEquals(seqNo, heartbeatMessageFromBytes.getSequenceNum());
    }



}
