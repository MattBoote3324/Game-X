package seng302.MessageTests;

import org.junit.Assert;
import org.junit.Test;
import seng302.Common.Messages.BoatActionMessage;
import seng302.Common.Messages.Message;

public class BoatActionMessageTest {

    @Test
    public void testBoatActionMessage(){
        int sourceID = 123;
        byte action = (byte) 5;
        BoatActionMessage boatActionMessage = new BoatActionMessage(sourceID, action);

        BoatActionMessage boatActionMessageFromBytes = new BoatActionMessage(new Message(boatActionMessage.getBytes()));

        Assert.assertEquals(sourceID, boatActionMessageFromBytes.getSourceID());
        Assert.assertEquals(action, boatActionMessageFromBytes.getAction());
    }
}
