package seng302;

import org.junit.Before;
import org.junit.Test;
import seng302.Client.Controllers.RaceViewController;
import seng302.Common.Boat;
import seng302.Server.Model.Wind;

import static org.junit.Assert.assertEquals;

/**
 * Test for Boat class
 * Created by mvj14 on 18/07/17.
 */
public class BoatTest {
    private Boat boat;
    private double expected;
    private double result;
    private Wind wind;

    @Before
    public void setup() {
        boat = new Boat(123, "This Sailing Boat", "TSB");
        wind = new Wind(12, 0);
    }

    @Test
    public void testUpdateBoatSpeed45() {
        wind.setCurrentSpeed(30.0);
        wind.setCurrentDirection(15.0);
        boat.setHeading(90.0);
        boat.setSpeed(10);
        boat.sailsIn(wind.getCurrentDirection(), wind.getCurrentSpeed());
        // Set to *0.2 to account for acceleration
        expected = 9 + 42.0 * 0.1;
        result = boat.getSpeed();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testUpdateBoatSpeed90() {
        wind.setCurrentSpeed(20.0);
        wind.setCurrentDirection(405.0);
        boat.setHeading(135.0);
        boat.setSpeed(10);
        boat.sailsIn(wind.getCurrentDirection(), wind.getCurrentSpeed());
        // Set to *0.2 to account for acceleration
        expected = 9 + 39.0 * 0.1;
        result = boat.getSpeed();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testUpdateBoatSpeed43() {
        wind.setCurrentSpeed(12.0);
        wind.setCurrentDirection(340.0);
        boat.setHeading(23.0);
        boat.setSpeed(10);
        boat.sailsIn(wind.getCurrentDirection(), wind.getCurrentSpeed());
        // Set to *0.2 to account for acceleration
        expected = 9 + 14.4 * 0.1;
        result = boat.getSpeed();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testTackingWind45() {
        wind.setCurrentDirection(45.0);
        boat.setTargetHeading(55.0);
        boat.tack(wind.getCurrentDirection());
        expected = 35.0;
        result = boat.getTargetHeading();
        assertEquals(expected,result,0.001);
    }

    @Test
    public void testTackingWind0() {

        wind.setCurrentDirection(0.0);
        boat.setTargetHeading(200);
        boat.tack(wind.getCurrentDirection());
        expected = 160;
        result = boat.getTargetHeading();
        assertEquals(expected,result,0.001);
    }

    @Test
    public void testTackingWind180() {

        wind.setCurrentDirection(180.0);
        boat.setTargetHeading(20);
        boat.tack(wind.getCurrentDirection());
        expected = 340;
        result = boat.getTargetHeading();
        assertEquals(expected,result,0.001);
    }

    @Test
    public void testBestAngle20() {
        wind.setCurrentDirection(0.0);
        wind.setCurrentSpeed(30.0);
        boat.setHeading(20.0);
        boat.determineWindSide(wind.getCurrentDirection());
        boat.setBestAngle(wind.getCurrentDirection(), wind.getCurrentSpeed());
        expected = 42.0;
        result = boat.getTargetHeading();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testBestAngle170() {
        wind.setCurrentDirection(0.0);
        wind.setCurrentSpeed(30.0);
        boat.setHeading(170);
        boat.determineWindSide(wind.getCurrentDirection());
        boat.setBestAngle(wind.getCurrentDirection(), wind.getCurrentSpeed());
        expected = 150.0;
        result = boat.getTargetHeading();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testBestAngle300() {
        wind.setCurrentDirection(0.0);
        wind.setCurrentSpeed(30.0);
        boat.determineWindSide(wind.getCurrentDirection());
        boat.setTargetHeading(300);
        boat.setBestAngle(wind.getCurrentDirection(), wind.getCurrentSpeed());
        expected = 318.0;
        result = boat.getTargetHeading();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testBestAngle260() {
        wind.setCurrentDirection(0.0);
        wind.setCurrentSpeed(30.0);
        boat.determineWindSide(wind.getCurrentDirection());
        boat.setHeading(259);
        boat.setBestAngle(wind.getCurrentDirection(), wind.getCurrentSpeed());
        expected = 210.0;
        result = boat.getTargetHeading();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testUpwindHeadingChange() {
        wind.setCurrentDirection(90.0);
        boat.setTargetHeading(50);
        boat.setSpeed(10);
        boat.upwindHeadingChange(wind.getCurrentDirection());
        expected = 53;
        result = boat.getTargetHeading();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testUpwindHeadingChange2DegreesFromUp() {
        wind.setCurrentDirection(90.0);
        boat.setHeading(92);
        boat.upwindHeadingChange(90);
        expected = 90;
        result = boat.getTargetHeading();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testUpwindHeadingChange180() {
        wind.setCurrentDirection(90.0);
        boat.setHeading(270);
        boat.setSpeed(10);
        boat.upwindHeadingChange(90);
        expected = 273;
        result = boat.getTargetHeading();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testDownwindHeadingChange() {
        wind.setCurrentDirection(90.0);
        boat.setHeading(50);
        boat.setSpeed(10);
        boat.downwindHeadingChange(90);
        expected = 47;
        result = boat.getTargetHeading();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testDownwindHeadingChange2DegreesFromUp() {
        wind.setCurrentDirection(90.0);
        boat.setHeading(272);
        boat.downwindHeadingChange(90);
        expected = 270;
        result = boat.getTargetHeading();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testDownwindHeadingChange180() {
        wind.setCurrentDirection(90.0);
        boat.setHeading(90);
        boat.setSpeed(10);
        boat.downwindHeadingChange(90);
        expected = 87;
        result = boat.getTargetHeading();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testClockwiseRotation() {
        boat.setHeading(90);
        boat.setSpeed(10);
        boat.clockwiseHeadingChange();
        expected = 93;
        result = boat.getTargetHeading();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testCounterClockwiseRotation() {
        boat.setHeading(90);
        boat.setSpeed(10);
        boat.counterClockwiseHeadingChange();
        expected = 87;
        result = boat.getTargetHeading();
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testDetermineOrder() {
        Boat boat1 = new Boat(1,"boat1", "1");
        Boat boat2 = new Boat(2,"boat2", "2");
        int res = 0;
        //Test when boat 2 is a leg ahead
        boat1.setCourseProgress(1);
        boat2.setCourseProgress(2);
        res = Boat.determineOrder(boat1, boat2);
        assertEquals(-1, res);

        //Test when boat 2 is a leg behind
        boat1.setCourseProgress(3);
        boat2.setCourseProgress(2);
        res = Boat.determineOrder(boat1, boat2);
        assertEquals(1, res);

        //Test when boats are on the same leg, boat 1 in front
        boat1.setCourseProgress(2);
        boat2.setCourseProgress(2);
        boat1.setDistanceToNextMark(4);
        boat2.setDistanceToNextMark(9);
        res = Boat.determineOrder(boat1, boat2);
        assertEquals(1, res);


        //Test when boats are on the same leg, boat 1 behind
        boat1.setCourseProgress(2);
        boat2.setCourseProgress(2);
        boat1.setDistanceToNextMark(9);
        boat2.setDistanceToNextMark(4);
        res = Boat.determineOrder(boat1, boat2);
        assertEquals(-1, res);

        //Test when boats are on the same leg, same position
        boat1.setCourseProgress(2);
        boat2.setCourseProgress(2);
        boat1.setDistanceToNextMark(4);
        boat2.setDistanceToNextMark(4);
        res = Boat.determineOrder(boat1, boat2);
        assertEquals(0, res);

    }
}

