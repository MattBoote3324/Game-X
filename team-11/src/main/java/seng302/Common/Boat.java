package seng302.Common;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import seng302.Client.ClientMain;
import seng302.Common.Utils.Calculator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.floorMod;
import static java.lang.Math.sqrt;

/**
 * A domain model class representing a Boat participating in an AC35 race
 * Stores data about the boat and it's progress during a race.
 */
public class Boat implements Comparable<Boat> {

    public static final double COLLISION_THRESHOLD = 0.000125;
    private static final double HEADING_CHANGE_SCALE = 0.2;
    private static final long APHRODITE_ABILITY_DURATION = 3000;
    public static final long ARES_ABILITY_DURATION = 3000;
 //   public static final double ARES_ABILITY_RADIUS_LATLONG = 9.994E-4; // This draws the Ares hit box. We changed it to match the  new updated courses.
    public static final double ARES_ABILITY_RADIUS_NAUTICAL_MILE = 0.06;
    private static final double TACKING_SPEED_SCALE = 0.2;
    private final int MAX_TRACK_SIZE = 50;
    private final int MAX_WAKE_SIZE = 250;


//    public static Comparator<Boat> BoatIndexComparator = (b1, b2) -> {
//        int compared = b1.compareTo(b2);
//        if (compared == 0) {
//            if (b1.getDistanceToNextMark() <= b2.getDistanceToNextMark()) {
//                return -10;
//            }
//        }
//        return compared;
//    };

    private final DoubleProperty speedProp= new SimpleDoubleProperty();
    private boolean qKeyAvailable = true;  // Set the key ability's true to start with
    private boolean wKeyAvailable = true;
    private int qTimeLeft = 0;
    private int wTimeLeft = 0;

    public final DoubleProperty speedProperty() {
        speedProp.set(speed);
        return speedProp;
    }

    private final Polar polar = new Polar(new PolarCSVReader("/csv/polarsAC35.csv"));
    private long lastUpdateTime;
    private double x;
    private double y;
    private int sourceID;

    private GreekGod greekGod;
    private ImageView godImage;

    private BoatStatus boatStatus;
    private int courseProgress = 0; // 0: Prestart. 1: From start to first mark. 2+: Sequential numbers as race proceeds
    private int numberPenaltiesAwarded;
    private int numberPenaltiesServed;
    private long estTimeToNextMark;
    private long estTimeToFinish;

    private double distanceToNextMark; //SERVER SIDE

    // The point that annotations should offset from.
    //TODO get rid of this
    private double annotationX;
    private double annotationY;

    private double latitude;
    private double longitude;
    private String name;
    private String abbreviatedName;
    private String godName;
    private double speed; // In knots.
    private double heading; // In degrees.
    private double targetHeading;
    private double timeToProgress = 0; // The time in milliseconds it took to reach the feature defined by legNumber.
    private boolean finished = false;
    private String placing = "-";
    private boolean withdrawn = false; // True if the boat withdraws from the race. Results in placing of "DNF".
    private int numBlm;
    private double timeSinceLastMark;
    private boolean sailsOut = true;
    private PropertyChangeSupport support; //used to notify when a boats race progress is changed.
    private Deque<Point> trackPoints = new ArrayDeque<>(MAX_TRACK_SIZE);
    private Deque<Point> wakePoints = new ArrayDeque<>(MAX_WAKE_SIZE);
    private WindSide windSide = WindSide.NULL;
    private Point lastPoint = new Point();
    // Defines how to draw the boat.
    private double radius;
    private Color fillColor = Color.BLACK;
    private Color strokeColor =  Color.WHITE;
    private static List<Color> fillColors = new ArrayList<>(
            Arrays.asList(Color.RED, Color.GREEN, Color.PURPLE, Color.PINK, Color.BLUE, Color.ORANGE, Color.YELLOW, Color.TEAL, Color.BLACK));

    private boolean assigned;
    private int sequenceNum; // Amount of boat location messages sent / received for this boat.
    private int trueWindDirection;

    private double lastLat;
    private double lastLong;
    private boolean inBounds = true;
    private boolean disable = false;

    //todo maybe move these somewhere better
    private boolean aphroditeAbilityInUse = false;
    private Point aphroditeAttractToPoint;
    private long aphroditeTimeOn;

    private boolean aresAbilityInUse = false;
    private long aresTimeOn;

    public Boat(int sourceID, String name, String abbreviatedName) {
        this.sourceID = sourceID;
        this.name = name;
        this.abbreviatedName = abbreviatedName;
        this.support = new PropertyChangeSupport(this);
        fillColor = fillColors.get(sourceID % fillColors.size());
        boatStatus = BoatStatus.RACING; //TODO: Consider this later.
    }

    /**
     * Determines which side of boat the wind is on (port, starboard)
     * @param windDirection wind direction in degrees
     */
    public void determineWindSide(double windDirection) {
        double trueWindAngle = Math.floorMod((long) (heading - windDirection), 360);
        if (trueWindAngle < 180 && trueWindAngle > 0) {
            windSide = WindSide.PORT;
        } else if (trueWindAngle > 180){
            windSide = WindSide.STARBOARD;
        }
    }

    /**
     * Update the boat speed based on heading, wind direction, and speed (in knots)
     *
     * Server side method.
     * @param windDirection wind direction in degrees
     * @param windSpeed speed of the wind in knots
     */
    public void updateSpeed(double windDirection, double windSpeed) {   // Wind speed in knots
        determineWindSide(windDirection);
        double accelerationFactor = 0.9;
        speed *= accelerationFactor;
        if (!sailsOut) {
            double trueWindAngle = Math.abs(heading % 360 - windDirection % 360);
            if (trueWindAngle > 180.0) {
                trueWindAngle = 360.0 - trueWindAngle;
            }
            if(isAresAbilityInUse()){
                speed += polar.getBoatSpeedAtTrueWindSpeed(windSpeed,polar.getBestDownwindAngleAtTrueWindSpeed(windSpeed))* (1 - accelerationFactor);
            }
            speed += polar.getBoatSpeedAtTrueWindSpeed(windSpeed, trueWindAngle) * (1 - accelerationFactor);
        }
    }

    /**
     * Update location based on speed and heading. Heading changes based on old heading
     * If boat is being affected by the aphrodite ability, boats move backwards toward last boat
     * Server side method.
     *
     * @param timeStamp timestamp in milliseconds?
     */
    public void updateLocation(long timeStamp) {
        double distTravelled = 0;
        Point updatedLocation;

        if (lastUpdateTime == 0) {
            lastUpdateTime = timeStamp;
        }

        lastPoint.setLatitude(latitude);
        lastPoint.setLongitude(longitude);

        double timeSinceLastUpdate = timeStamp - lastUpdateTime;
        timeSinceLastUpdate = timeSinceLastUpdate / 3600.0 / 1000.0; // Converting milliseconds to hours.

        if(!aphroditeAbilityInUse){ //if the aphrodite W ability isn't activated, move boats normally
            long tackingSpeed = (long) (1 + TACKING_SPEED_SCALE * speed); //proportional to boat speed
            // Calculate new heading based on old heading and target heading
            if (abs(targetHeading - heading) < tackingSpeed) {
                heading = targetHeading;
            } else if (Math.floorMod((long) (targetHeading - heading), 360) > 180) {
                heading = floorMod((long) heading - tackingSpeed, 360);
            } else {
                heading = floorMod((long) heading + tackingSpeed, 360);
            }
            // Distance travelled since lastUpdateTime.
            distTravelled = speed * 20 * timeSinceLastUpdate;
            updatedLocation = Calculator.projectedPoint(latitude, longitude, distTravelled, heading);
        } else { //if the aphrodite W ability is activated, move boats backwards
            distTravelled = 100 * timeSinceLastUpdate;
            updatedLocation = Calculator.projectedPoint(latitude, longitude, distTravelled, Calculator.angleBetweenTwoPoints(lastPoint, aphroditeAttractToPoint));
            if(timeStamp >= aphroditeTimeOn){
                aphroditeAbilityInUse = false;
            }
        }

        latitude = updatedLocation.getLatitude();
        longitude = updatedLocation.getLongitude();

        lastUpdateTime = timeStamp;
    }

    /**
     * Updates the heading of the boat in the upwind direction
     * If the boat is already travelling upwind, heading remains the same
     * @param windDirection for getting direction of the wind
     */
    public void upwindHeadingChange(double windDirection) {
        determineWindSide(windDirection);
        long headingChange = (long) (1 + HEADING_CHANGE_SCALE * speed);
        double trueWindAngle = Math.floorMod((long) (targetHeading - windDirection), 360);
        double newHeading;

        if (trueWindAngle <= 180) {
            if (trueWindAngle < 3) {
                newHeading = windDirection;
            } else if (trueWindAngle == 180) {
                if (windSide == WindSide.STARBOARD) {
                    newHeading = Math.floorMod((long) (targetHeading - headingChange), 360);
                } else {
                    newHeading = Math.floorMod((long) (targetHeading + headingChange), 360);
                }
            } else {
                newHeading = Math.floorMod((long) (targetHeading - headingChange), 360);
            }
        } else {
            if (trueWindAngle > 357) {
                newHeading = windDirection;
            } else {
                newHeading = Math.floorMod((long) (targetHeading + headingChange), 360);
            }
        }
        targetHeading = newHeading;
    }

    /**
     * Updates the heading of the boat in the downwind direction
     * If the boat is already travelling downwind, heading remains the same
     * @param windDirection for getting direction of the wind
     */
    public void downwindHeadingChange(double windDirection) {
        determineWindSide(windDirection);
        long headingChange = (long) (1 + HEADING_CHANGE_SCALE * speed);
        double downwind = Math.floorMod((long) (windDirection + 180), 360);
        double trueWindAngle = Math.floorMod((long) (targetHeading - windDirection), 360);
        double newHeading;

        if (trueWindAngle < 180) {
            if (trueWindAngle > 177) {
                newHeading = downwind;
            } else if (trueWindAngle == 0) {
                if (windSide == WindSide.STARBOARD) {
                    newHeading = Math.floorMod((long) (targetHeading + headingChange), 360);
                } else {
                    newHeading = Math.floorMod((long) (targetHeading - headingChange), 360);
                }
            } else {
                newHeading = Math.floorMod((long) (targetHeading + headingChange), 360);
            }
        }else{
            if (trueWindAngle < 183) {
                newHeading = downwind;
            } else {
                newHeading = Math.floorMod((long) (targetHeading - headingChange), 360);
            }
        }
        targetHeading = newHeading;
    }

    /**
     * Rotate the boat clockwise proportionally to the boat speed
     */
    public void clockwiseHeadingChange() {
        long headingChange = (long) (1 + HEADING_CHANGE_SCALE * speed);
        targetHeading = floorMod((long) targetHeading + headingChange, 360);
    }

    /**
     * Rotate the boat counter-clockwise proportionally to the boat speed
     */
    public void counterClockwiseHeadingChange() {
        long headingChange = (long) (1 + HEADING_CHANGE_SCALE * speed);
        targetHeading = floorMod((long) targetHeading - headingChange, 360);
    }

    public GreekGod getGreekGod() {
        return greekGod;
    }

    /**
     * Compares two boats based on their position in the race
     * @param b boat to compare to this
     * @return integer -1, 0, 1 from determineOrder stating order of the two boats
     */
    @Override
    public int compareTo(Boat b) {

        Course course = ClientMain.getRace().getCourse(); //todo is accessing course here really bad??

        Point boatPoint = b.toPoint();
        Point thisPoint = this.toPoint();


        int boatsNextFeatureId;
        int thisNextFeatureId;


        if(b.isFinished()){
            boatsNextFeatureId = course.getCourseOrder().get(b.getCourseProgress()).getCourseFeatureID();
            thisNextFeatureId = course.getCourseOrder().get(this.getCourseProgress()).getCourseFeatureID();
        }else{
            boatsNextFeatureId = course.getCourseOrder().get(b.getCourseProgress() + 1).getCourseFeatureID();
            thisNextFeatureId = course.getCourseOrder().get(this.getCourseProgress() + 1).getCourseFeatureID();
        }

        Point boatNext = course.getCourseFeatureById(boatsNextFeatureId).getMidPoint();
        Point thisNext = course.getCourseFeatureById(thisNextFeatureId).getMidPoint();

        b.setDistanceToNextMark(Calculator.distanceBetweenPoints(boatNext, boatPoint));
        this.setDistanceToNextMark(Calculator.distanceBetweenPoints(thisNext, thisPoint));

        return determineOrder(this, b);

    }

    /**
     * Method to determine the order of two boats in race standings
     * First compares the leg they are on, if on same leg, checks distance to next mark
     * @param b1 boat 1 to compare
     * @param b2 boat 2 to compare
     * @return int -1 for before, 0 for equal, 1 for after
     */
    public static int determineOrder(Boat b1, Boat b2){
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (b1.getCourseProgress() < b2.getCourseProgress()) {
            return BEFORE;
        } else if (b1.getCourseProgress() > b2.getCourseProgress()){
            return AFTER;
        } else {
            if(b2.getDistanceToNextMark() < b1.getDistanceToNextMark()) return BEFORE;
            else if(b2.getDistanceToNextMark() > b1.getDistanceToNextMark()) return AFTER;
            else return EQUAL;
        }
    }



    @Override
    public String toString() {
        return name;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void sailsIn(double windDirection, double windSpeed) {
        setIsSailsOut(false);
        updateSpeed(windDirection, windSpeed);
    }

    /**
     * Sets the sales out
     *
     * @param windDirection windDirection in degrees
     * @param windSpeed     windspeed in knots
     */
    public void sailsOut(double windDirection, double windSpeed) {
        setIsSailsOut(true);
        updateSpeed(windDirection, windSpeed);
    }

    /**
     * Flip the boat's bearing along wind direction, preserves true wind angle
     * @param windDirection wind direction in degrees
     */
    public void tack(double windDirection) {
        targetHeading = Math.floorMod((long) (windDirection * 2 - targetHeading), 360);
        if (windSide == WindSide.PORT) {
            windSide = WindSide.STARBOARD;
        } else if (windSide == WindSide.STARBOARD) {
            windSide = WindSide.PORT;
        } else {
            determineWindSide(windDirection);
        }
    }

    /**
     * Check if the boat has collided with a point in the given collision map.
     * If there is a collision, handle it.
     *
     * @param collisionPoints A list of points to check for collision
     */
    public void checkForPointCollision(List<Point> collisionPoints, double pointSize) {
        // 1 metre is 0.000009 degrees approx.
        double boatLength = 0.0005;
        double boatWidth = 0.0003;

        Point boatBow = Calculator.projectedPoint(latitude, longitude, boatLength / 2.0, heading);
        Point boatStern = Calculator.projectedPoint(latitude, longitude, boatLength / 2.0, heading + 180);

        for (Point point : collisionPoints) {
            double distanceToSide = abs((boatStern.getLatitude() - boatBow.getLatitude()) * point.getLongitude() -
                    (boatStern.getLongitude() - boatBow.getLongitude()) * point.getLatitude() +
                    boatStern.getLongitude() * boatBow.getLatitude() - boatBow.getLongitude() * boatStern.getLatitude()) /
                    sqrt(Math.pow(boatStern.getLatitude() - boatBow.getLatitude(), 2) + Math.pow(boatStern.getLongitude() - boatBow.getLongitude(), 2));
            double distanceToFront = Calculator.distanceBetweenAPointAndBoat(point, this);
            if (distanceToSide < boatWidth / 2.0 + pointSize && distanceToFront < boatLength + pointSize) {
                handleCollision(point);
            }
        }
    }

    /**
     * Check for mark collision with course features. Generates a list of feature points
     * @param courseFeatures The list of course features in the course
     */
    public void checkForMarkCollision(List<CourseFeature> courseFeatures) {
        List<Point> points = new ArrayList<>();
        for (CourseFeature feature : courseFeatures) {
            for (Point point : feature.getPoints()) {
                points.add(Calculator.projectedPoint(point.getLatitude(), point.getLongitude(), 0.0003, 0));
            }
        }
        checkForPointCollision(points, 0.0003);
    }

    /**
     * Handles a Collision for a given point
     * @param collisionPoint point that is in Collision
     */
    void handleCollision(Point collisionPoint) {
    //Fine collision detection
        double jumpScale = 0.01 + 0.005 * speed; //scale to move the boat away from the mark (1 is double the current distance)
        double speedScale = -1;//scale to multiply the current boat speed by
        double headingScale = 0.01 * speed;//scale to move the heading away from the mark (1 is double the current difference)
        longitude += jumpScale * (longitude - collisionPoint.getLongitude());
        latitude += jumpScale * (latitude - collisionPoint.getLatitude());
        speed = speedScale * speed;
        double headingToMark = Math.toDegrees(Math.atan2(collisionPoint.getLongitude() - longitude,
                collisionPoint.getLatitude() - latitude));
        setHeading(heading + headingScale * (Math.floorMod((long) (heading - headingToMark + 180), 360) - 180));
    }

    public Point toPoint() {
        return new Point(latitude, longitude, sourceID);
    }

    /**
     * I don't know what this does, so someone should document it!!!
     * @param point point to go around?
     * @return point
     */
    static Point tempRound(Point point) {
        return new Point(COLLISION_THRESHOLD * Math.round(point.getLatitude() / COLLISION_THRESHOLD),
                COLLISION_THRESHOLD * Math.round(point.getLongitude() / COLLISION_THRESHOLD));
    }



    /**
     * set the boat on the best VMG based on if it is going up or downwind
     * and which side of the boat the wind is coming from.
     *
     * @param windDirection wind direction in degrees
     * @param windSpeed windspeed in knots
     */
    public void setBestAngle(double windDirection, double windSpeed) {
        // SERVER SIDE METHOD.

        double deadZoneAngle = 10.0; // this is the angle of where nothing will happen if you try to set the best angle
        double boatWindAngle = (double) floorMod((long)(heading - windDirection), 360);

        // finds the side where the wind is coming from and the direction of the boat (upwind/ downwind)
        double bestAngle = -1;
        if (boatWindAngle < (90 - deadZoneAngle) || boatWindAngle > (270 + deadZoneAngle)) {
            bestAngle = polar.getBestUpwindAngleAtTrueWindSpeed(windSpeed);
        } else if (boatWindAngle > (90 + deadZoneAngle) && boatWindAngle < (270 - deadZoneAngle)) {
            bestAngle = polar.getBestDownwindAngleAtTrueWindSpeed(windSpeed);
        }
        if (windSide == WindSide.PORT && bestAngle != -1) {
            targetHeading = Math.floorMod((long) (windDirection + bestAngle), 360);
        } else if (bestAngle != -1) {
            targetHeading = Math.floorMod((long) (windDirection - bestAngle), 360);
        }
    }

    /**
     * Sets if the boat is within the bounds of the course
     * @param inBounds set state of boat vs bounds
     */
    void setInBounds(boolean inBounds) {
        this.inBounds = inBounds;
    }

    public boolean getInBounds() {
        return inBounds;
    }

    void setDisable(boolean disable) {
        this.disable = disable;

        if(disable){
           speed = 0;
        }
    }

    public boolean isDisable() {
        return disable;
    }

    void setLast() {
        lastLat = this.latitude;
        lastLong = this.longitude;

    }

    double getLastLat() {
        return lastLat;
    }

    double getLastLong() {
        return lastLong;
    }

    /**
     * Returns if this boat can use qAbility
     *
     * @return true if boat can use ability
     */
    public boolean canUseQ() {
        return qKeyAvailable;
    }

    /**
     * Resets the q cool down ability back to true
     */
    public void qSetAvailable() {
        qKeyAvailable = true;
    }

    /**
     * Sets the q Ability unavailable
     */
    public void qSetUnavailable() {
        qKeyAvailable = false;
    }

    /**
     * Sets the q Ability Available
     */
    public void wSetAvailable() {
        wKeyAvailable = true;
    }

    /**
     * Sets the w Ability unavailable
     */
    public void wSetUnavailable() {
        wKeyAvailable = false;
    }

    /**
     * Returns if this boat can use qAbility
     *
     * @return true if boat can use ability
     */
    public boolean canUseW() {
        return wKeyAvailable;
    }

    /**
     * Sets the amount of time left in the cooldown period for
     * the q ability
     *
     * @param timeElapsed time in seconds
     */
    public void setqTimeLeft(int timeElapsed) {
        qTimeLeft = timeElapsed;
    }

    /**
     * Returns the amount of time left in cool down period for
     * the q ability
     *
     * @return int of time left in seconds
     */
    public int qTimeLeft() {
        return qTimeLeft;
    }

    /**
     * Sets the amount of time left in the cooldown period for
     * the q ability
     *
     * @param timeElapsed time in seconds
     */
    public void setwTimeLeft(int timeElapsed) {
        wTimeLeft = timeElapsed;
    }

    /**
     * Returns the amount of time left in cool down period for
     * the q ability
     *
     * @return int of time left in seconds
     */
    public int wTimeLeft() {
        return wTimeLeft;
    }

    public enum BoatStatus {
        UNDEFINED(0, "Undefined"),
        PRESTART(1, "Prestart"),
        RACING(2, "Racing"),
        FINISHED(3, "Finished"),
        DNS(4, "Did Not Start"),
        DNF(5, "Did Not Finish"),
        DSQ(6, "Disqualified"),
        OCS(7, "On Course Side - across start line early");

        private String statusType;
        private int value;
        private boolean disable;

        BoatStatus(int i, String type) {
            statusType = type;
            value = i;
        }

        public static String getString(int raceStatus) {
            BoatStatus bs = BoatStatus.values()[raceStatus];
            return bs.toString();
        }

        public String toString() {
            return statusType;
        }

        public int getValue() {return value;}
    }

    /**
     * adds a trackpoint to a list of points and removes the oldest if the length of the
     * list is greater than MAX_TRACK_SIZE
     * @param point position of the boat
     */
    void addTrackPoint(Point point) {
        if(this.trackPoints.size() >= MAX_TRACK_SIZE){
            this.trackPoints.removeFirst();
        }
        this.trackPoints.offerLast(point);
    }

    /**
     * adds a wakepoint to a list of points and removes the oldest if the length of the
     * list is greater than MAX_WAKE_SIZE
     * @param point position of the boat
     */
    void addWakePoint(Point point) {
        if(this.wakePoints.size() >= MAX_WAKE_SIZE){
            this.wakePoints.removeFirst();
        }
        this.wakePoints.offerLast(point);
    }
    ////////////////////////////////////////////
    // Only getters and setters from here on. //
    ////////////////////////////////////////////

    public void setBoatStatus(int i, String type) {
        boatStatus = BoatStatus.values()[i];
    }

    public void setBoatStatus(BoatStatus status) {
        boatStatus = status;
    }

    public WindSide getWindSide() {
        return windSide;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public BoatStatus getBoatStatus() {
        return boatStatus;
    }

    public int getNumberPenaltiesAwarded() {
        return numberPenaltiesAwarded;
    }

    public void setNumberPenaltiesAwarded(int pen) {
        numberPenaltiesAwarded = pen;
    }

    public int getNumberPenaltiesServed() {
        return numberPenaltiesServed;
    }

    public void setNumberPenaltiesServed(int pen) {
        numberPenaltiesServed = pen;
    }

    public long getEstTimeToNextMark() {
        return estTimeToNextMark;
    }

    public void setEstTimeToNextMark(long estTime) {
        estTimeToNextMark = estTime;
    }

    public long getEstTimeToFinish() {
        return estTimeToFinish;
    }

    public void setEstTimeToFinish(long estTime) {
        estTimeToFinish = estTime;
    }

    public double getAnnotationX() {
        return annotationX;
    }

    public void setAnnotationX(double annotationX) {
        this.annotationX = annotationX;
    }

    public double getAnnotationY() {
        return annotationY;
    }

    public void setAnnotationY(double annotationY) {
        this.annotationY = annotationY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String getAbbreviatedName() {
        return abbreviatedName;
    }

    void setAbbreviatedName(String abbreviatedName) {
        this.abbreviatedName = abbreviatedName;
    }

    public String getNameAndAbbreviated() {
        return name + " (" + abbreviatedName + ")";
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getStringSpeed() {
        return String.format("%.1f", speed);
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = (double) Math.floorMod((long) heading, 360); //must do it this way because java produces negatives when using mod(%)
        targetHeading = this.heading;
    }

    public int getCourseProgress() {
        return courseProgress;
    }

    public void setCourseProgress(int legNumber) {
        int oldprogress = this.courseProgress;
        this.courseProgress = legNumber;
        if (support !=null ){
            support.firePropertyChange("legNumber", oldprogress, this.courseProgress); // notifies the sparklines so that
        }

    }

    private double getTimeToProgress() {
        return timeToProgress;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        //this.speed = 0;
        this.finished = finished;
    }

    public String getPlacing() {
        return placing;
    }

    public void setPlacing(String placing) {
        this.placing = placing;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public boolean isWithdrawn() {
        return withdrawn;
    }

    public int getSourceID() {
        return sourceID;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    int getNumBlm() {
        return numBlm;
    }

    public void setNumBlm(int numBlm) {
        this.numBlm = numBlm;
    }

    public boolean isSailsOut() {
        return sailsOut;
    }

    public void setIsSailsOut(boolean sailsOut) {
        this.sailsOut = sailsOut;
    }

    /**
     * Set the time since the last mark back to zero
     */
    public void resetTimeSinceLastMark() {
        // resets to the current time
        timeSinceLastMark = System.currentTimeMillis();
    }

    /**
     * sets the time since the boat last when round the mark
     */
    public void updateTimeSinceLastMark() {
        //TODO - Make this here
    }

    /**
     * Gets the time since the last mark
     * @return time since last mark
     */
    long getTimeSinceLastMark() {
        return (long) ((System.currentTimeMillis() - timeSinceLastMark) / 1000.0);
    }

    public Deque<Point> getTrackPoints() {
        return trackPoints;
    }
    public Deque<Point> getWakePoints() {
        return wakePoints;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public double getTargetHeading() {
        return targetHeading;
    }

    public void setTargetHeading(double targetHeading) {
        this.targetHeading = targetHeading;
    }

    public void setGreekGod(GreekGod greekGod) {
        this.greekGod = greekGod;
        this.godImage = new ImageView(greekGod.getGodIcon());
    }
    public ImageView getGodImage(){
        return godImage;
    }

    public double getDistanceToNextMark() {
        return distanceToNextMark;
    }

    public void setDistanceToNextMark(double distanceToNextMark) {
        this.distanceToNextMark = distanceToNextMark;
    }

    public boolean isAphroditeAbilityInUse() {
        return aphroditeAbilityInUse;
    }

    void setAphroditeAbilityInUse(boolean aphroditeAbilityInUse) {
        this.aphroditeTimeOn = System.currentTimeMillis() + APHRODITE_ABILITY_DURATION;
        this.aphroditeAbilityInUse = aphroditeAbilityInUse;
    }

    void setAphroditeAttractToPoint(Point aphroditeAttractToPoint) {
        this.aphroditeAttractToPoint = aphroditeAttractToPoint;
    }

    public void setAresAbilityInUse(boolean aresAbilityInUse) {
        this.aresTimeOn = System.currentTimeMillis() + ARES_ABILITY_DURATION;
        this.aresAbilityInUse = aresAbilityInUse;
    }
    public long getAresTimeOn(){
        return this.aresTimeOn;
    }

    public boolean isAresAbilityInUse() {
        return aresAbilityInUse;
    }

    public String getGodName(){
        return this.greekGod.getGodType().toString();
    }
}

