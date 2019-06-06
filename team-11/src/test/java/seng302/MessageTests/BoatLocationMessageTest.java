package seng302.MessageTests;

import org.junit.Assert;
import org.junit.Test;
import seng302.Common.Boat;
import seng302.Common.Messages.BoatLocationMessage;
import seng302.Common.Utils.Calculator;

public class BoatLocationMessageTest {

    @Test
    public void testBoatLocationMessage(){
        Boat boat = new Boat(5, "Test Boat", "TB");
        boat.setHeading(55);
        boat.setLatitude(166);
        boat.setLongitude(-66);
        boat.setSpeed(51);
        BoatLocationMessage boatLocationMessage = new BoatLocationMessage(boat);

        BoatLocationMessage boatLocationMessageFromBytes = new BoatLocationMessage(boatLocationMessage);

        Assert.assertEquals(boat.getSourceID(), boatLocationMessageFromBytes.getSourceID());
        Assert.assertEquals(boat.getHeading(), Calculator.hexToDirection(boatLocationMessageFromBytes.getHeading()), 0.01);
        Assert.assertEquals(boat.getLatitude(), Calculator.hexToLatLon(boatLocationMessageFromBytes.getLatitude()), 0.01);
        Assert.assertEquals(boat.getLongitude(), Calculator.hexToLatLon(boatLocationMessageFromBytes.getLongitude()), 0.01);
        Assert.assertEquals(boat.getSpeed(), Calculator.speedMmsToKnots(boatLocationMessageFromBytes.getBoatSpeed()), 0.01);
    }
}