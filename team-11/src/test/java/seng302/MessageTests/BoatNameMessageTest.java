package seng302.MessageTests;

import org.junit.Assert;
import org.junit.Test;
import seng302.Common.Messages.BoatNameMessage;


/**
 * Created by gmc125 on 28/08/17.
 * Tests for the boat customization message
 */
public class BoatNameMessageTest {
    private int sourceID = 150;
    private String abbrevName = "NZL";
    private String boatName = "Bob the Builder";

    @Test
    public void testBoatSourceIdMessage() {

        BoatNameMessage boatNameMsg = new BoatNameMessage(sourceID, abbrevName, boatName);

        BoatNameMessage rxBoatNameMsg = new BoatNameMessage(boatNameMsg);

        Assert.assertEquals(sourceID, rxBoatNameMsg.getSourceID());

    }

    @Test
    public void testBoatShortNameMessage() {

        BoatNameMessage boatNameMsg = new BoatNameMessage(sourceID, abbrevName, boatName);

        BoatNameMessage rxBoatNameMsg = new BoatNameMessage(boatNameMsg);

        Assert.assertEquals(abbrevName, rxBoatNameMsg.getShortName());

    }

    @Test
    public void testBoatLongNameMessage() {

        BoatNameMessage boatNameMsg = new BoatNameMessage(sourceID, abbrevName, boatName);

        BoatNameMessage rxBoatNameMsg = new BoatNameMessage(boatNameMsg);

        Assert.assertEquals(boatName, rxBoatNameMsg.getBoatName());
    }


}
