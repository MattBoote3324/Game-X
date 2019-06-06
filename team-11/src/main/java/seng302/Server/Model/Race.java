package seng302.Server.Model;

import seng302.Common.*;
import seng302.Common.Utils.Calculator;
import seng302.Server.Tasks.WindGenerationTask;

import java.util.*;

import static java.lang.Math.abs;

public class Race extends Observable {
    private static final int NEXT_MARK = 1;
    private static final int NEXT_NEXT_MARK = 2;
    private static final double PASS_ANGLE_OF_MARK = 10;
    private static final double GRID_NORTH = 180;
    private Wind wind = new Wind(11, 225);  // Initial wind settings - these can be changed if required
    private Course course;

    public Map<Boat, Boolean> getMarkCheckPoints() {
        return markCheckPoints;
    }

    private Map<Boat, Boolean> markCheckPoints = new HashMap<>();

    private RaceStatus raceState = RaceStatus.NOT_ACTIVE;
    private Fleet fleet;
    private Regatta regatta;
    private long expectedStartTime;

    public Race() {
    }

    /**
     * Starts the random wind generation in the Wind object
     * @param taskTimer the timer which runs this task
     */
    public void initWindGenerator(Timer taskTimer) {
        taskTimer.schedule(new WindGenerationTask(wind), 0, 500);
    }

    /**
     * Determines if the boat has passed a course feature or not
     * If the feature is a gate, it checks which side of the gate the boat is on
     * If the feature is a mark, then it uses two angles to determine in order which ones the boat has gone past
     *
     * Then updates the boats progress
     *
     * @param boat boat to check progress on
     */
    public void updateBoatProgress(Boat boat) {
        if (this.raceState == RaceStatus.STARTED) { // Detect rounding only after the race state has started
            // Check we're not at the end of the features first ...
            if (boat.getCourseProgress() < course.getCourseFeatures().size()) {
                // Get a list of ordered features
                Map<Integer, CourseCheckPoint> orderedFeatures = course.getCourseOrder();

                // Boat index is off by one due to mapping, so always have to add one to the course progress
                int featureID = orderedFeatures.get(boat.getCourseProgress() + NEXT_MARK).getCourseFeatureID();
                CourseFeature feature = course.getCourseFeatureById(featureID);
                String rounding = orderedFeatures.get(boat.getCourseProgress() + NEXT_MARK).getRounding();

                Point a = feature.getPoint1();
                // If the feature type is a mark, handle it differently
                if (feature.getType() == FeatureType.MARK) {
                    // Get the relative position of the boat to the mark
                    double boatDeltaY = boat.getLongitude() - a.getLongitude();
                    double boatDeltaX = boat.getLatitude() - a.getLatitude();

                    // work out the angle from the mark to the boat
                    double angle = -Math.toDegrees(Math.atan2(boatDeltaY, boatDeltaX));

                    // get angle from previous course feature to the boat
                    int prevFeatureID = orderedFeatures.get(boat.getCourseProgress()).getCourseFeatureID();
                    CourseFeature previousFeature = course.getCourseFeatureById(prevFeatureID);
                    double angleFromPrevious = getAngleFrom(previousFeature, a);

                    // Get angle to next course feature from the boat
                    int nextFeatureID = orderedFeatures.get(boat.getCourseProgress() + NEXT_NEXT_MARK).getCourseFeatureID();
                    CourseFeature nextFeature = course.getCourseFeatureById(nextFeatureID);
                    double angleToNext = getAngleTo(nextFeature, a);

                    // Mark has two "pass" lines that the boat must pass in order.
                    // Check that our boat has gone past one leg of the mark
                    if (!markCheckPoints.get(boat)) {
                        // work out the relative mark angles to pass
                        if (abs(angle - angleFromPrevious) < PASS_ANGLE_OF_MARK) {
                            markCheckPoints.put(boat, true);
                        }
                    }

                    // Has it been thru the 2nd "pass" line
                    if (markCheckPoints.get(boat)) {
                        if (abs(angle - angleToNext) < PASS_ANGLE_OF_MARK) {
                            markCheckPoints.put(boat, false);
                            // We have passed by each of the defined points of the mark
                            boat.setCourseProgress(boat.getCourseProgress() + 1);
                        }
                    }
                } else {
                    // get the two points that make up the feature point
                    Point b = feature.getPoint2();
                    // Make a new point for where the boat is
                    Point boatPoint = boat.toPoint();
                    // Do a line check to see which side of the line the boat is on
                    boolean overGateLine = doLineCheck(a, b, boatPoint);

                    boolean betweenPoints = checkBetweenPoints(a, b, boatPoint);

                    // If the position is true, then we are on the "right hand" side of the line, therefor have crossed it
                    double dy = b.getLongitude() - a.getLongitude();
                    // Get the change in x
                    double dx = b.getLatitude() - a.getLatitude();
                    // work out the angle from the mark to the boat
                    double angle = -Math.toDegrees(Math.atan2(dy, dx));

                    if (overGateLine && betweenPoints) {
                        // Check which side the mark should be passed on.
                        // Looks for a difference between the angle and the heading to decide which side the mark is on of the boat
                        if (rounding.equals("PS") && Math.floorMod((long) (GRID_NORTH - angle - boat.getHeading()), 360) > GRID_NORTH) {
                            boat.setCourseProgress(boat.getCourseProgress() + 1);
                        } else if (rounding.equals("SP") && Math.floorMod((long) (GRID_NORTH - angle - boat.getHeading()), 360) < GRID_NORTH) {
                            boat.setCourseProgress(boat.getCourseProgress() + 1);
                        }
                    }
                }
            } else {
                boat.setBoatStatus(Boat.BoatStatus.FINISHED);
                boat.setFinished(true);
                //boat.setSpeed(0);
            }
        }
    }

    /**
     * Get angle To a feature given a point
     *
     * @param feature feature to measure angle from
     * @param a       point to angle
     * @return angle in degrees
     */
    private double getAngleTo(CourseFeature feature, Point a) {
        double X = -feature.getPoint1().getLongitude() + a.getLongitude();
        double Y = -feature.getPoint1().getLatitude() + a.getLatitude();
        return Math.toDegrees(Math.atan2(Y, X));
    }

    /**
     * Get angle from a feature given a point
     *
     * @param feature feature to measure angle from
     * @param a       point to angle
     * @return angle in degrees
     */
    private double getAngleFrom(CourseFeature feature, Point a) {
        double X = feature.getPoint1().getLongitude() - a.getLongitude();
        double Y = feature.getPoint1().getLatitude() - a.getLatitude();
        return Math.toDegrees(Math.atan2(Y, X));
    }

    /**
     * returns a boolean if the distance between points ac + bc are equal to ab
     * Used a scaling factor to make comparing doubles easier
     *
     * @param a point a
     * @param b point b
     * @param c point c
     * @return true if approx less than the distance, false if the distance isn't approx the same
     */
    private boolean checkBetweenPoints(Point a, Point b, Point c) {
        double ac = distance(a, c) * 100000;
        double bc = distance(b, c) * 100000;
        double ab = (distance(a, b) * 100000) + 3;
        double acbc = ac + bc;

        return Double.compare(acbc, ab) < 0;
    }

    /**
     * Calculates the distance between two points
     *
     * @param a point a
     * @param b point b
     * @return distance between those points
     */
    private double distance(Point a, Point b) {
        return Math.sqrt(Math.pow(b.getLatitude() - a.getLatitude(), 2) +
                Math.pow(b.getLongitude() - a.getLongitude(), 2));
    }

    /**
     * Sets the race state
     *
     * @param raceState state of the race
     */
    public void setRaceState(RaceStatus raceState) {
        this.raceState = raceState;
    }


    /**
     * Calculates given 2 points if the boat is on the left or right side of the line
     *
     * @param a point a
     * @param b point b
     * @param c point c
     * @return true if the boat is on the right hand side of the line
     */
    private boolean doLineCheck(Point a, Point b, Point c) {
        // formula for line check (b.x - a.x)*(c.y - a.y) > (b.y - a.y)*(c.x - a.x);

        return ((b.getLatitude() - a.getLatitude()) * (c.getLongitude() - a.getLongitude()))
                > ((b.getLongitude() - a.getLongitude()) * (c.getLatitude() - a.getLatitude()));
    }

    public Fleet getFleet() {
        return fleet;
    }

    public void setFleet(Fleet fleet) {
        this.fleet = fleet;
        markCheckPoints = new HashMap<>();
        for (Boat boat : fleet.getBoats()) {
            markCheckPoints.put(boat, false);
        }
        setChanged();
        notifyObservers();
    }

    public RaceStatus getRaceState() {
        return raceState;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setRegatta(Regatta reg){
        this.regatta = reg;
    }

    public void setExpectedStartTime(long expectedStartTime) {
       this.expectedStartTime = expectedStartTime;
    }

    public long getExpectedStartTime() {
        return expectedStartTime;
    }

    public double getWindDirection() {
        return wind.getCurrentDirection();
    }

    /**
     * Setters used by client when receiving race status messages with wind info
     * @param windDirection Wind direction as an angle (0-359 degrees)
     */
    public void setWindDirection(double windDirection) {
        wind.setCurrentDirection(windDirection);
    }

    public double getWindSpeed() {
        return wind.getCurrentSpeed();
    }

    /**
     * Setters used by client when receiving race status messages with wind info
     * @param windSpeed Wind speed in knots (conversion from mm/sec handled within client main?
     */
    public void setWindSpeed(double windSpeed) {
        wind.setCurrentSpeed(windSpeed);
    }
}