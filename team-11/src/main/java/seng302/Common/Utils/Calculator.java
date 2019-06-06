package seng302.Common.Utils;

import javafx.collections.ObservableList;
import seng302.Common.Boat;
import seng302.Common.Course;
import seng302.Common.CourseFeature;
import seng302.Common.Point;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.round;

/**
 * Created by mvj14 on 19/07/17
 */
public class Calculator {
    private static final double KNOTS_TO_MMS_RATIO = 514 + 4.0/9.0;

    /**
     * Get the point on Earth a specified distance away from another point at a specified bearing.
     *
     * @param latitude Initial latitude.
     * @param longitude Initial longitude.
     * @param distance Distance to travel in nautical miles.
     * @param bearing Bearing from starting point in degrees.
     * @return The projected point.
     */
    public static Point projectedPoint(double latitude, double longitude, double distance, double bearing) {
        double radiusOfEarth = 6371;
        double bearingRad = Math.toRadians(bearing);
        double distanceKm = distance * 1.852;

        double latitudeRad = Math.toRadians(latitude);
        double longitudeRad = Math.toRadians(longitude);

        double projectedLat = Math.asin(Math.sin(latitudeRad) * Math.cos(distanceKm / radiusOfEarth) +
            Math.cos(latitudeRad) * Math.sin(distanceKm / radiusOfEarth) * Math.cos(bearingRad));
        double projectedLon = longitudeRad + Math.atan2(Math.sin(bearingRad) * Math.sin(distanceKm / radiusOfEarth) *
            Math.cos(latitudeRad), Math.cos(distanceKm / radiusOfEarth) - Math.sin(latitudeRad) * Math.sin(projectedLat));

        projectedLat = Math.toDegrees(projectedLat);
        projectedLon = Math.toDegrees(projectedLon);

        return new Point(projectedLat, projectedLon);
    }

    /**
     * Get the distance between two points, in nautical miles.
     *
     * @param point1 Point 1.
     * @param point2 Point 2.
     * @return The distance between the two given points, in nautical miles.
     */
    public static double distanceBetweenPoints(Point point1, Point point2) {
        double R = 6371e3; // Radius of Earth in metres.
        // Haversine formula
        double lat1 = Math.toRadians(point1.getLatitude());
        double lat2 = Math.toRadians(point2.getLatitude());
        double latDifference = Math.toRadians(point2.getLatitude() - point1.getLatitude());
        double lonDifference = Math.toRadians(point2.getLongitude() - point1.getLongitude());

        double a = Math.sin(latDifference / 2) * Math.sin(latDifference / 2) +
            Math.cos(lat1) * Math.cos(lat2) * Math.sin(lonDifference / 2) * Math.sin(lonDifference / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distMetres = R * c;
        return distMetres / 1852; // Convert metres to nautical miles.
    }

    /**
     * Calculate bearing between two points (from point1 to point2)
     * @param point1 Point with long and lat
     * @param point2 Another Point with long and lat
     * @return bearing
     */
    public static double bearingBetweenPoints(Point point1, Point point2) {
        double lat1 = Math.toRadians(point1.getLatitude());
        double lat2 = Math.toRadians(point2.getLatitude());
        double long1 = Math.toRadians(point1.getLongitude());
        double long2 = Math.toRadians(point2.getLongitude());

        double x = Math.cos(lat1) * Math.sin(lat2)
                - Math.sin(lat1) * Math.cos(lat2)
                * Math.cos(long2 - long1);
        double y = Math.sin(long2 - long1) * Math.cos(lat2);

        double bearing = Math.atan2(y, x);

        return Math.floorMod((long) Math.toDegrees(bearing), 360);
    }

    /**
     * Get the mid point between two points.
     *
     * @param point1 Point 1.
     * @param point2 Point 2.
     * @return The mid point.
     */
    public static Point midPoint(Point point1, Point point2) {
        double latRad1 = Math.toRadians(point1.getLatitude());
        double latRad2 = Math.toRadians(point2.getLatitude());
        double lonRad1 = Math.toRadians(point1.getLongitude());
        double lonRad2 = Math.toRadians(point2.getLongitude());

        double Bx = Math.cos(latRad2) * Math.cos(lonRad2 - lonRad1);
        double By = Math.cos(latRad2) * Math.sin(lonRad2 - lonRad1);

        double midLatRad = Math.atan2(
            Math.sin(latRad1) + Math.sin(latRad2),
            Math.sqrt((Math.cos(latRad1) + Bx) * (Math.cos(latRad1) + Bx) + By * By)
        );
        double midLonRad = lonRad1 + Math.atan2(By, Math.cos(latRad1) + Bx);

        double midLat = Math.toDegrees(midLatRad);
        double midLon = Math.toDegrees(midLonRad);
        return new Point(midLat, midLon);
    }

    /**
     * Finds the distance between a boat and a midpoint(Lat and Long)
     * @param point midpoint between the two marks / the mark itself
     * @param boat the boat to Find the distance between
     * @return distance
     */
    public static double distanceBetweenAPointAndBoat(Point point, Boat boat){
        //Point midPoint = feature.getMidPoint();
        double changeX = Math.pow(point.getLongitude() - boat.getLongitude(), 2);
        double changeY = Math.pow(point.getLatitude() - boat.getLatitude(), 2);

        return Math.sqrt(changeX + changeY);
    }

    /**
     * Finds the boat in last place based on course progress and distance from the next mark
     * @param boats ObservableList of boats
     * @param course the course object
     * @return The boat in last place
     */
    public static Boat boatInLastPlace(ObservableList<Boat> boats, Course course){
        Boat behindBoat = boats.get(0);
        for(Boat boat: boats){
            if(!boat.isFinished()) {

                Point boatPoint = boat.toPoint();
                Point nextPoint = course.getCourseFeatureById(boat.getCourseProgress() + 1).getMidPoint();
                boat.setDistanceToNextMark(Calculator.distanceBetweenPoints(nextPoint, boatPoint));

                if (behindBoat.getCourseProgress() > boat.getCourseProgress()) {
                    behindBoat = boat;
                } else if (behindBoat.getCourseProgress() == boat.getCourseProgress() && behindBoat.getDistanceToNextMark() < boat.getDistanceToNextMark()) {
                    behindBoat = boat;
                }
            }
        }
        return behindBoat;
    }


    /**
     * Convert latitude or longitude to a hex integer, as required by the AC35 interface specifications.
     *
     * @param value The latitude or longitude value to convert to an integer.
     * @return The converted integer form of the given double value.
     */
    public static int latLonToHex(double value) {
        return (int) ((value * Math.pow(2, 31)) / 180.0);
    }

    /**
     * Convert latitude or longitude to meaningful values from a hex integer.
     *
     * @param value The latitude or longitude as a hex integer.
     * @return The converted latitude or longitude.
     */
    public static double hexToLatLon(int value) {
        return (value / Math.pow(2, 31) * 180.0);
    }

    /**
     * Convert a plain direction value in the range (0, 360] to an integer in the range (0x0000, 0xFFFF),
     * as required by the AC35 interface specifications.
     *
     * @param direction The direction value to convert to hex.
     * @return The converted hex form of the given direction value.
     */
    public static int directionToHex(double direction) {
        return (int) (direction * Math.pow(2, 16) / 360.0);
    }

    /**
     * Convert an integer in the range (0x0000, 0xFFFF)
     * to a plain direction value in the range (0, 360].
     *
     * @param direction The hex value to convert to an plain direction.
     * @return The converted direction form of the given hex value.
     */
    public static double hexToDirection(int direction) {
        return round(((direction & 0xFFFF) * 360.0 / Math.pow(2, 16)));
    }

    /**
     * Convert speed from Knots to mm/s
     *
     * @param knotsSpeed The Knots speed value to convert to mm/s.
     * @return The converted speed in mm/s.
     */
    public static int speedKnotsToMms(double knotsSpeed) {
        return (int) (knotsSpeed * KNOTS_TO_MMS_RATIO);
    }

    /**
     * Convert speed from mm/s to Knots.
     *
     * @param mmsSpeed The mm/s speed value to convert to Knots.
     * @return The converted speed in mm/s.
     */
    public static double speedMmsToKnots(int mmsSpeed) {
        return mmsSpeed / KNOTS_TO_MMS_RATIO;
    }

    /**
     * Generate a new point by rounding the position of
     * a given point to the nearest given threshold.
     *
     * @param point The point to round from.
     * @param threshold The threshold defining what to round to.
     * @return The rounded point.
     */
    public static Point roundPointPosition(Point point, double threshold) {
        double roundedLat = threshold * (double) Math.round(point.getLatitude() / threshold);
        double roundedLon = threshold * (double) Math.round(point.getLongitude() / threshold);
        return new Point(roundedLat, roundedLon);
    }

    /**
     * Generates the angle between two points(lat and Long)
     * @param point1 the first Lat and Long point
     * @param point2 The Second Lat and Long Point
     * @return Angle between the two Points in degrees.
     */
    public static double angleBetweenTwoPoints(Point point1, Point point2){
        double vec2y = point2.getLongitude() - point1.getLongitude() ;
        double vec2x = point2.getLatitude() - point1.getLatitude();
        double angle = Math.toDegrees(Math.atan2(vec2y, vec2x));
        return angle;
    }

    /**
     * Takes in an int and returns the correct ordinal suffix as a string
     * @param i int to get ordinal for
     * @return ordinal of given number as string
     */
    public static String ordinal(int i) {
        String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return "th";
            default:
                return sufixes[i % 10];

        }
    }
}