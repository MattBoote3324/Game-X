package seng302;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import seng302.Client.Controllers.CourseController;
import seng302.Common.*;
import seng302.Common.Utils.Calculator;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static org.junit.Assert.assertEquals;

/**
 * Test Calculator methods.
 */
public class CalculatorTest {

    @Test
    public void projectedPointTest1() {
        double latitude = -43.5;
        double longitude = 172.6;
        double distance = 409.0;
        double bearing = 15.0;

        double expectedLatitude = -36.89778;
        double expectedLongitude = 174.8;

        Point projectedPoint = Calculator.projectedPoint(latitude, longitude, distance, bearing);

        assertEquals(expectedLatitude, projectedPoint.getLatitude(), 0.0005);
        assertEquals(expectedLongitude, projectedPoint.getLongitude(), 0.0005);
    }

    @Test
    public void projectedPointTest2() {
        double latitude = 35.7;
        double longitude = -199.7;
        double distance = 16.5;
        double bearing = 164.0;

        double expectedLatitude = 35.43583;
        double expectedLongitude = -199.6069;

        Point projectedPoint = Calculator.projectedPoint(latitude, longitude, distance, bearing);

        assertEquals(expectedLatitude, projectedPoint.getLatitude(), 0.0005);
        assertEquals(expectedLongitude, projectedPoint.getLongitude(), 0.0005);
    }

    @Test
    public void testDistanceBetweenPoints1() {
        Point point1 = new Point(50.5, -12);
        Point point2 = new Point(12, 30);
        Assert.assertEquals(3090.7, Calculator.distanceBetweenPoints(point1, point2), 3090.7 * 0.001);
    }

    @Test
    public void testDistanceBetweenPoints2() {
        Point point1 = new Point(0, 0);
        Point point2 = new Point(180, 0);
        Assert.assertEquals(10809.9, Calculator.distanceBetweenPoints(point1, point2), 10809.935 * 0.001);
    }

    @Test
    public void testMidPoint1() {
        Point point1 = new Point(10, 20);
        Point point2 = new Point(40, 50);
        Assert.assertEquals(25.757, Calculator.midPoint(point1, point2).getLatitude(), 0.001);
        Assert.assertEquals(33.083, Calculator.midPoint(point1, point2).getLongitude(), 0.001);
    }

    @Test
    public void testMidPoint2() {
        Point point1 = new Point(10, 20);
        Point point2 = new Point(-100, 155.5);
        Assert.assertEquals(-36.028, Calculator.midPoint(point1, point2).getLatitude(), 0.001);
        Assert.assertEquals(13.735, Calculator.midPoint(point1, point2).getLongitude(), 0.001);
    }

    @Test
    public void testLatLonToHex1() {
        Assert.assertEquals(Integer.MAX_VALUE, Calculator.latLonToHex(180), 0);
    }

    @Test
    public void testLatLonToHex2() {
        Assert.assertEquals(Integer.MIN_VALUE / 2.0, Calculator.latLonToHex(-90), 0);
    }

    @Test
    public void testHexToLatLon1() {
        Assert.assertEquals(180, Calculator.hexToLatLon(Integer.MAX_VALUE), 0.0001);
    }

    @Test
    public void testHexToLatLon2() {
        Assert.assertEquals(-90, Calculator.hexToLatLon(Integer.MIN_VALUE / 2), 0.0001);
    }

    @Test
    public void testDirectionToHex1() {
        Assert.assertEquals(0xC000, Calculator.directionToHex(270));
    }

    @Test
    public void testDirectionToHex2() {
        Assert.assertEquals(0x6000, Calculator.directionToHex(135));
    }

    @Test
    public void testHexToDirection1() {
        Assert.assertEquals(270, Calculator.hexToDirection(0xC000), 0.0001);
    }

    @Test
    public void testHexToDirection2() {
        Assert.assertEquals(135, Calculator.hexToDirection(0x6000), 0.0001);
    }

    @Test
    public void testSpeedKnotsToMms() {
        Assert.assertEquals(17408, Calculator.speedKnotsToMms(33.84));
    }

    @Test
    public void testSpeedMmsToKnots() {
        Assert.assertEquals(33.84, Calculator.speedMmsToKnots(17408), 0.01);
    }

    @Test
    public void testRoundPointPosition1() {
        Point point = new Point(123.4, -66.123);
        double threshold = 0.5;
        Point expectedPoint = new Point(123.5, -66.0);
        Assert.assertEquals(expectedPoint, Calculator.roundPointPosition(point, threshold));
    }

    @Test
    public void testRoundPointPosition2() {
        Point point = new Point(154.19, 23.89);
        double threshold = 0.11;
        Point expectedPoint = new Point(154.22, 23.87);
        Assert.assertEquals(expectedPoint, Calculator.roundPointPosition(point, threshold));
    }

    @Test
    public void testRoundPointPosition3() {
        double threshold = 0.025;
        Point point1 = new Point(166.1312, -33.669);
        Point point2 = new Point(166.1179, -33.676);
        Assert.assertEquals(Calculator.roundPointPosition(point1, threshold), Calculator.roundPointPosition(point2, threshold));
    }

    @Test
    public void testAngleBetween2Points(){
        double threshold = 0.025;

        Point point1 = new Point(0.0, 0.0);
        Point point2 = new Point(5.0, 5.0);
        Point point3 = new Point(0.0, 5.0);
        Point point4 = new Point(5.0, 0.0);

        Assert.assertEquals(45.0, Calculator.angleBetweenTwoPoints(point1, point2), threshold);
        Assert.assertEquals(-135.0, Calculator.angleBetweenTwoPoints(point2, point1), threshold);
        Assert.assertEquals(0.0, Calculator.angleBetweenTwoPoints(point1, point4), threshold);
        Assert.assertEquals(90.0, Calculator.angleBetweenTwoPoints(point1, point3), threshold);
        Assert.assertEquals(-90.0, Calculator.angleBetweenTwoPoints(point3, point1), threshold);
    }

    @Test
    public void testDistanceBetweenAPointAndBoat(){
        double threshold = 0.025;
        Point point1 = new Point(0.0, 0.0);
        Boat boat1 = new Boat(1, "test", "test");

        boat1.setLatitude(0.0);
        boat1.setLongitude(1.0);
        Assert.assertEquals(1.0, Calculator.distanceBetweenAPointAndBoat(point1, boat1), threshold);

        boat1.setLatitude(0.0);
        boat1.setLongitude(1.0);
        Assert.assertEquals(1.0, Calculator.distanceBetweenAPointAndBoat(point1, boat1), threshold);

        boat1.setLatitude(2.0);
        boat1.setLongitude(2.0);
        Assert.assertEquals(2.828, Calculator.distanceBetweenAPointAndBoat(point1, boat1), threshold);

        boat1.setLatitude(-2.0);
        boat1.setLongitude(-2.0);
        Assert.assertEquals(2.828, Calculator.distanceBetweenAPointAndBoat(point1, boat1), threshold);

        boat1.setLatitude(2.0);
        boat1.setLongitude(-2.0);
        Assert.assertEquals(2.828, Calculator.distanceBetweenAPointAndBoat(point1, boat1), threshold);

    }

    /**
     * Tests edge cases for the ordinal method
     */
    @Test
    public void testOrdinalsEdgeCases(){

        assertEquals(Calculator.ordinal(0), "th");
        assertEquals(Calculator.ordinal(1), "st");
        assertEquals(Calculator.ordinal(2), "nd");
        assertEquals(Calculator.ordinal(3), "rd");
        assertEquals(Calculator.ordinal(4), "th");
        assertEquals(Calculator.ordinal(5), "th");
        assertEquals(Calculator.ordinal(11), "th");
        assertEquals(Calculator.ordinal(12), "th");
        assertEquals(Calculator.ordinal(13), "th");
        assertEquals(Calculator.ordinal(14), "th");
        assertEquals(Calculator.ordinal(20), "th");
        assertEquals(Calculator.ordinal(21), "st");
        assertEquals(Calculator.ordinal(22), "nd");
        assertEquals(Calculator.ordinal(23), "rd");
        assertEquals(Calculator.ordinal(24), "th");
        assertEquals(Calculator.ordinal(101), "st");
        assertEquals(Calculator.ordinal(102), "nd");
        assertEquals(Calculator.ordinal(103), "rd");
        assertEquals(Calculator.ordinal(104), "th");
        assertEquals(Calculator.ordinal(111), "th");
        assertEquals(Calculator.ordinal(112), "th");
        assertEquals(Calculator.ordinal(113), "th");
        assertEquals(Calculator.ordinal(114), "th");


    }
}
