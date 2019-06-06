package seng302;

import org.junit.Assert;
import org.junit.Test;
import seng302.Common.Messages.BoatAvatarMessage;


/**
 * Created by gmc125 on 28/08/17.
 * Tests for the boat customization message
 */
public class BoatAvatarMessageTest {
    private int sourceID = 150;
    private int avatarType = 2;

    @Test
    public void testBoatAvatarIdMessage() {

        BoatAvatarMessage boatAvatarMsg = new BoatAvatarMessage(sourceID, avatarType);

        BoatAvatarMessage rxBoatAvatarMsg = new BoatAvatarMessage(boatAvatarMsg);

        Assert.assertEquals(avatarType, rxBoatAvatarMsg.getType());
        Assert.assertNotEquals(avatarType + 1, rxBoatAvatarMsg.getType());
    }
}
