package seng302.MessageTests;

import org.junit.Assert;
import org.junit.Test;
import seng302.Common.Boat;
import seng302.Common.BoatStatusWrapper;
import seng302.Common.Fleet;
import seng302.Common.Messages.Message;
import seng302.Common.Messages.RaceStatusMessage;
import seng302.Common.RaceStatus;
import seng302.Common.Utils.Calculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RaceStatusMessageTest {
    /**
     * Runs a test on the RaceStatusGeneration. Packs the class with test data,
     * then passes it back to the same class as new data to unpack it.
     * If they match - then we have packed and unpacked the data correctly.
     */
    @Test
    public void testRaceStatusMessageGeneration() {
        ArrayList<Boat> competingBoats = new ArrayList<>(Arrays.asList(
                new Boat(1, "ORACLE TEAM USA", "USA"),
                new Boat(2, "Artemis Racing", "SWE"),
                new Boat(3, "Emirates Team New Zealand", "NZL"),
                new Boat(4, "Groupama Team France", "FRA"),
                new Boat(5, "Land Rover BAR", "GBR"),
                new Boat(6, "SoftBank Team Japan", "JAP")
        ));

        Fleet fleet = new Fleet(competingBoats);

        for (Boat boat : fleet.getBoats()) {
            boat.setBoatStatus(Boat.BoatStatus.RACING);
            boat.setCourseProgress(3);
            boat.setNumberPenaltiesAwarded(2);
            boat.setNumberPenaltiesServed(2);
            boat.setEstTimeToNextMark(0);
            boat.setEstTimeToFinish(0);
        }

        // set race progress to started
        RaceStatus raceStatus = RaceStatus.STARTED;
        long startTime = System.currentTimeMillis();
        int windDir = 181;
        int windSpeed = Calculator.speedKnotsToMms(42);
        int boatsInRace = competingBoats.size();
        short raceType = 2; // fleet type

        // Create me a new Status message with preset variables
        RaceStatusMessage rs = new RaceStatusMessage(raceStatus, startTime, windDir, windSpeed, raceType, fleet);
        // Generate a bunch of bytes

        // message body should be 144 bytes
        assertEquals(144, rs.getBody().length);

        // Create a new "Received" Race status message
        RaceStatusMessage rxd = new RaceStatusMessage(new Message(rs.getBytes()));

        // Run tests to make sure all values were unpacked ok
        assertEquals(raceStatus.getValue(), rxd.getRaceStatus().getValue());
        // Cos someone removed the ability to give the Status message the current time, we have to ask the packet
        // what time it was sent on
        assertEquals(rs.getCurrentTime(), rxd.getCurrentTime());
        assertEquals(startTime, rxd.getExpectedStartTime());
        assertEquals(windDir, Calculator.hexToDirection(rxd.getCourseWindDirection()), 1);
        assertEquals(boatsInRace, rxd.getNumBoatsInRace());
        assertEquals(raceType, rxd.getRaceType());
        assertEquals(competingBoats.get(0).getCourseProgress(), rxd.getBoatStatuses().get(1).getLegNumber());
    }

	@Test
	public void testRaceStatusMessage(){
		RaceStatus raceStatus = RaceStatus.STARTED;
		long expectedStartTime = System.currentTimeMillis();
		double courseWindDirection = 61;
		double courseWindSpeed = 11;
		int raceType = 8;
		Fleet fleet = new Fleet();

		Boat boat1 = new Boat(1, "Test Boat 1", "TB1");
		boat1.setHeading(55);
		boat1.setLatitude(166);
		boat1.setLongitude(-66);
		boat1.setSpeed(51);
		boat1.setCourseProgress(5);

		Boat boat2 = new Boat(2, "Test Boat 2", "TB2");
		boat2.setHeading(20);
		boat2.setLatitude(165);
		boat2.setLongitude(-69);
		boat2.setSpeed(25);
		boat2.setCourseProgress(2);

		fleet.addBoat(boat1);
		fleet.addBoat(boat2);

		RaceStatusMessage raceStatusMessage = new RaceStatusMessage(raceStatus, expectedStartTime, courseWindDirection, courseWindSpeed, raceType, fleet);

		RaceStatusMessage raceStatusMessageFromBytes = new RaceStatusMessage(raceStatusMessage);

		Assert.assertEquals(raceStatus, raceStatusMessageFromBytes.getRaceStatus());
		Assert.assertEquals(expectedStartTime, raceStatusMessageFromBytes.getExpectedStartTime());
		Assert.assertEquals(courseWindDirection, Calculator.hexToDirection(raceStatusMessageFromBytes.getCourseWindDirection()), 0.01);
		Assert.assertEquals(courseWindSpeed, Calculator.speedMmsToKnots(raceStatusMessageFromBytes.getCourseWindSpeed()), 0.01);
		Assert.assertEquals(raceType, raceStatusMessageFromBytes.getRaceType());

		Map<Integer, BoatStatusWrapper> boatStatuses = raceStatusMessageFromBytes.getBoatStatuses();

		Assert.assertEquals(boat1.getCourseProgress(), boatStatuses.get(1).getLegNumber());
		Assert.assertEquals(boat2.getCourseProgress(), boatStatuses.get(2).getLegNumber());
	}
}